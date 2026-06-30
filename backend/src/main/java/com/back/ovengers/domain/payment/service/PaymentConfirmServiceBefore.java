package com.back.ovengers.domain.payment.service;

import com.back.ovengers.domain.payment.client.TossPaymentClient;
import com.back.ovengers.domain.payment.dto.PaymentConfirmRequest;
import com.back.ovengers.domain.payment.dto.PaymentResponse;
import com.back.ovengers.domain.payment.dto.TossConfirmResponse;
import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [개선 전 - BEFORE]
 *
 * 비관적 락을 잡은 단일 트랜잭션 안에서 외부 API(토스) 호출까지 수행.
 * 락을 거는 방식은 운영 코드(PaymentService.preConfirm)와 동일한 패턴을 따른다.
 *   1) orderId로 Payment 조회 (락 없음)
 *   2) 그 Payment의 Reservation을 락 (findByIdWithLock - ReservationRepository)
 *   3) 같은 Reservation의 모든 Payment를 락 조회 (findAllByReservationIdWithLock)
 *
 * 문제점:
 *  - 위 락 획득 시점부터 트랜잭션이 끝날 때까지 락이 유지된다.
 *  - 그 사이에 tossPaymentClient.confirm() 호출이 끼어 있어,
 *    토스 응답이 늦어질수록(네트워크 레이턴시) 락 홀딩 시간이 그만큼 늘어난다.
 *
 * 성능 비교 측정용으로만 사용 — 실제 운영 코드 아님.
 */
@Service
@RequiredArgsConstructor
public class PaymentConfirmServiceBefore {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final TossPaymentClient tossPaymentClient;

    @Transactional
    public PaymentResponse confirm(PaymentConfirmRequest request) {

        // 1. orderId로 결제 조회 (락 없음 — reservationId를 얻기 위함)
        Payment target = paymentRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        Long reservationId = target.getReservation().getId();

        // 2. Reservation 락 — 여기서부터 트랜잭션 끝까지 락이 유지된다
        Reservation reservation = reservationRepository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // 3. 같은 Reservation의 모든 Payment를 락 조회 (current read 보장)
        List<Payment> lockedPayments = paymentRepository.findAllByReservationIdWithLock(reservationId);
        Payment payment = lockedPayments.stream()
                .filter(p -> p.getOrderId().equals(request.orderId()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 4. 검증
        if (payment.getStatus() != PaymentStatus.READY) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
        if (!payment.getPaidPrice().equals(request.amount())) {
            throw new CustomException(ErrorCode.AMOUNT_MISMATCH);
        }

        // 5. 토스 API 호출 — ★ 락을 잡은 채로 외부 호출 (문제의 핵심) ★
        //    네트워크 레이턴시가 곧 락 홀딩 시간 연장으로 직결된다.
        TossConfirmResponse tossResponse = tossPaymentClient.confirm(
                request.paymentKey(), request.orderId(), request.amount()
        );

        // 6. DB 갱신 — 이 시점까지 락이 계속 유지되고 있었음
        payment.confirm(request.paymentKey());

        return PaymentResponse.of(payment, reservation);
    }
}
