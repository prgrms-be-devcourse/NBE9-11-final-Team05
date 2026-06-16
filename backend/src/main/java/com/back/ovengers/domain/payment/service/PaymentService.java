package com.back.ovengers.domain.payment.service;

import com.back.ovengers.domain.payment.dto.PaymentRequest;
import com.back.ovengers.domain.payment.dto.PaymentResponse;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

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

        // 4. 이미 결제된 예약인지 확인
        if (paymentRepository.findByReservation_Id(request.getReservationId()).isPresent()) {
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

    // 결제 완료 후 예약 상태 원자적 처리
    public void confirm(String orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 결제 상태 변경
        payment.updateStatus(PaymentStatus.DONE);

        // 예약 상태 변경 (같은 트랜잭션에서 처리)
        payment.getReservation().updateStatus(ReservationStatus.CONFIRMED);
    }
}
