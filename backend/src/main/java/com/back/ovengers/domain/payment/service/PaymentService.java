package com.back.ovengers.domain.payment.service;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.notification.entity.NotificationType;
import com.back.ovengers.domain.notification.service.NotificationService;
import com.back.ovengers.domain.payment.client.TossPaymentClient;
import com.back.ovengers.domain.payment.dto.PaymentConfirmRequest;
import com.back.ovengers.domain.payment.dto.PaymentConfirmResponse;
import com.back.ovengers.domain.payment.dto.PaymentRequest;
import com.back.ovengers.domain.payment.dto.PaymentResponse;
import com.back.ovengers.domain.payment.dto.TossConfirmResponse;
import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.event.PaymentCompletedEvent;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final TossPaymentClient tossPaymentClient;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    // self-injection: 같은 클래스 내부 @Transactional 메소드 호출 시 프록시 우회 방지
    @Lazy
    @Autowired
    private PaymentService self;

    @Transactional
    public PaymentResponse create(Long userId, PaymentRequest request) {

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.RESERVATION_FORBIDDEN);
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        boolean alreadyPaid = paymentRepository.findAllByReservation_Id(request.reservationId())
                .stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.DONE);
        if (alreadyPaid) {
            throw new CustomException(ErrorCode.ALREADY_PAID);
        }

        String prefix = RandomStringUtils.randomAlphabetic(3).toUpperCase();
        String orderId = prefix + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(orderId)
                .paidPrice(reservation.getRsvPrice())
                .status(PaymentStatus.READY)
                .build();

        return PaymentResponse.of(paymentRepository.save(payment), reservation);
    }

    // ─────────────────────────────────────────────
    // 결제 승인 (오케스트레이션, 트랜잭션 없음)
    //
    // 락 홀딩 최소화를 위해 3단계로 분리:
    //   preConfirm  [TX+락] 검증 + IN_PROGRESS 선점 → 커밋하며 락 해제
    //   Toss confirm [락 밖] 느려도 다른 요청 안 막음 (Toss는 멱등)
    //   postConfirm [TX+락] DONE + 예약 CONFIRMED 확정
    //
    // NOTE: Toss 승인 직후 서버 다운 시 IN_PROGRESS 잔여 건 발생 가능.
    //       실서비스라면 Toss 상태 조회 기반 reconcile 배치 필요.
    //       본 프로젝트 범위에서는 타임아웃 + releaseConfirm으로 대응.
    // ─────────────────────────────────────────────
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {

        // 1단계: 검증 + 선점 (짧은 TX)
        Long paymentId = self.preConfirm(request);

        // 2단계: Toss 호출 (락/트랜잭션 밖)
        TossConfirmResponse tossResponse;
        try {
            tossResponse = tossPaymentClient.confirm(
                    request.paymentKey(),
                    request.orderId(),
                    request.amount()
            );
        } catch (Exception e) {
            // 실패 시 선점 해제 (IN_PROGRESS → READY 복구, 재시도 가능)
            self.releaseConfirm(paymentId);
            throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        // 3단계: 최종 반영 (짧은 TX)
        return self.postConfirm(paymentId, request.paymentKey(), tossResponse);
    }

    // 1단계: 락 + 검증 + 선점
    @Transactional
    public Long preConfirm(PaymentConfirmRequest request) {

        Payment payment = paymentRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        Reservation reservation = reservationRepository
                .findByIdWithLock(payment.getReservation().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.ALREADY_PAID);
        }

        // 락 조회로 같은 예약의 모든 결제를 current-read
        List<Payment> lockedPayments = paymentRepository.findAllByReservationIdWithLock(reservation.getId());

        // 자기 자신을 포함해 최신 상태 확인
        Payment lockedSelf = lockedPayments.stream()
                .filter(p -> p.getId().equals(payment.getId()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 내 payment(최신값)가 이미 진행/완료면 차단
        if (lockedSelf.getStatus() == PaymentStatus.DONE
                || lockedSelf.getStatus() == PaymentStatus.IN_PROGRESS) {
            throw new CustomException(ErrorCode.ALREADY_PAID);
        }

        // 다른 결제가 진행/완료 중이어도 차단
        boolean otherActive = lockedPayments.stream()
                .filter(p -> !p.getId().equals(payment.getId()))
                .anyMatch(p -> p.getStatus() == PaymentStatus.IN_PROGRESS
                        || p.getStatus() == PaymentStatus.DONE);
        if (otherActive) {
            throw new CustomException(ErrorCode.ALREADY_PAID);
        }

        if (lockedSelf.getStatus() != PaymentStatus.READY) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
        if (!lockedSelf.getPaidPrice().equals(request.amount())) {
            throw new CustomException(ErrorCode.AMOUNT_MISMATCH);
        }

        // 선점 (락 조회로 읽은 lockedSelf를 변경 — 영속 상태라 dirty checking 됨)
        lockedSelf.updateStatus(PaymentStatus.IN_PROGRESS);

        return lockedSelf.getId();
    }

    // 3단계: 락 + 최종 반영
    @Transactional
    public PaymentConfirmResponse postConfirm(
            Long paymentId, String paymentKey, TossConfirmResponse tossResponse) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 다시 락 획득 (cancel 등과 직렬화)
        Reservation reservation = reservationRepository
                .findByIdWithLock(payment.getReservation().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        payment.confirm(paymentKey);
        reservation.updateStatus(ReservationStatus.CONFIRMED);

        // 같은 예약의 다른 READY 결제 정리
        paymentRepository.findAllByReservation_Id(reservation.getId()).stream()
                .filter(p -> !p.getId().equals(payment.getId()))
                .filter(p -> p.getStatus() == PaymentStatus.READY)
                .forEach(p -> p.updateStatus(PaymentStatus.CANCELLED));

        Camping camping = reservation.getSite().getCamping();
        eventPublisher.publishEvent(
                new PaymentCompletedEvent(
                        reservation.getId(),
                        reservation.getUser().getId(),
                        camping.getHost().getId(),
                        camping.getName()
                )
        );

        notificationService.send(
                reservation.getUser(),
                NotificationType.PAYMENT_DONE,
                "결제가 완료되었습니다."
        );

        return PaymentConfirmResponse.of(payment, tossResponse.method(), tossResponse.approvedAt());
    }

    // 보상: Toss 실패 시 선점 해제
    @Transactional
    public void releaseConfirm(Long paymentId) {
        paymentRepository.findById(paymentId).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.IN_PROGRESS) {
                p.updateStatus(PaymentStatus.READY);
            }
        });
    }
}
