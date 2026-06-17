package com.back.ovengers.domain.payment.service;

import com.back.ovengers.domain.payment.client.TossPaymentClient;
import com.back.ovengers.domain.payment.dto.*;
import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final TossPaymentClient tossPaymentClient;

    @Transactional
    public PaymentResponse create(Long userId, PaymentRequest request) {

        // 1. 예약 조회
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // 2. 본인 예약인지 확인
        if (!reservation.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.RESERVATION_FORBIDDEN);
        }

        // 3. 예약 상태 확인 (PENDING일 때만 결제 가능)
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        // 4. 이미 결제된 예약인지 확인, DONE 상태 결제가 있으면 차단
        List<Payment> existingPayments = paymentRepository.findAllByReservation_Id(request.getReservationId());
        boolean alreadyPaid = existingPayments.stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.DONE);

        if (alreadyPaid) {
            throw new CustomException(ErrorCode.ALREADY_PAID);
        }

        // 5. 주문번호 생성
        String prefix = RandomStringUtils.randomAlphabetic(3).toUpperCase();
        String orderId = prefix + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 6. 결제 생성
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(orderId)
                .paidPrice(reservation.getRsvPrice())
                .status(PaymentStatus.READY)
                .build();

        return PaymentResponse.of(paymentRepository.save(payment), reservation);
    }


    // 토스페이먼츠 결제 승인
    @Transactional
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {

        // 1. 결제 조회
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 2. 이미 완료된 결제인지 확인
        if (payment.getStatus() == PaymentStatus.DONE) {
            throw new CustomException(ErrorCode.ALREADY_PAID);
        }

        // 3. 금액 검증
        if (!payment.getPaidPrice().equals(request.getAmount())) {
            throw new CustomException(ErrorCode.AMOUNT_MISMATCH);
        }

        // 4. 토스페이먼츠 승인 API 호출
        TossConfirmResponse tossResponse;
        try {
            tossResponse = tossPaymentClient.confirm(
                    request.getPaymentKey(),
                    request.getOrderId(),
                    request.getAmount()
            );
        } catch (CustomException e) {
            throw e;
        }

        // 5. DB 반영 (실패 시 망취소 처리)
        try {
            payment.confirm(request.getPaymentKey());
            payment.getReservation().updateStatus(ReservationStatus.CONFIRMED);

            // 5-1. 같은 예약의 나머지 READY 결제 정리
            List<Payment> otherPayments = paymentRepository.findAllByReservation_Id(
                    payment.getReservation().getId());

            otherPayments.stream()
                    .filter(p -> !p.getId().equals(payment.getId()))
                    .filter(p -> p.getStatus() == PaymentStatus.READY)
                    .forEach(p -> p.updateStatus(PaymentStatus.CANCELLED));
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            tossPaymentClient.cancel(request.getPaymentKey(), "서버 오류로 인한 자동 취소");
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return PaymentConfirmResponse.of(payment, tossResponse.getMethod(), tossResponse.getApprovedAt());
    }
}
