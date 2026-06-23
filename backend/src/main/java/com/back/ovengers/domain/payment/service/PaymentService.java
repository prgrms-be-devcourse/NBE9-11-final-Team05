package com.back.ovengers.domain.payment.service;

import com.back.ovengers.domain.notification.entity.NotificationType;
import com.back.ovengers.domain.notification.service.NotificationService;
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
    private final NotificationService notificationService;

    @Transactional
    public PaymentResponse create(Long userId, PaymentRequest request) {

        Reservation reservation = reservationRepository.findById(request.reservationId())  // getReservationId() -> reservationId()
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.RESERVATION_FORBIDDEN);
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        List<Payment> existingPayments = paymentRepository.findAllByReservation_Id(request.reservationId());  // getReservationId() -> reservationId()
        boolean alreadyPaid = existingPayments.stream()
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

    @Transactional
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {

        Payment payment = paymentRepository.findByOrderId(request.orderId())  // getOrderId() -> orderId()
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.DONE) {
            throw new CustomException(ErrorCode.ALREADY_PAID);
        }

        if (!payment.getPaidPrice().equals(request.amount())) {  // getAmount() -> amount()
            throw new CustomException(ErrorCode.AMOUNT_MISMATCH);
        }

        TossConfirmResponse tossResponse;
        try {
            tossResponse = tossPaymentClient.confirm(
                    request.paymentKey(),  // getPaymentKey() -> paymentKey()
                    request.orderId(),     // getOrderId() -> orderId()
                    request.amount()       // getAmount() -> amount()
            );
        } catch (CustomException e) {
            throw e;
        }

        try {
            payment.confirm(request.paymentKey());  // getPaymentKey() -> paymentKey()
            payment.getReservation().updateStatus(ReservationStatus.CONFIRMED);

            List<Payment> otherPayments = paymentRepository.findAllByReservation_Id(
                    payment.getReservation().getId());

            otherPayments.stream()
                    .filter(p -> !p.getId().equals(payment.getId()))
                    .filter(p -> p.getStatus() == PaymentStatus.READY)
                    .forEach(p -> p.updateStatus(PaymentStatus.CANCELLED));

            // 5-2. 유저에게 결제 완료 알림
            notificationService.send(
                    payment.getReservation().getUser(),
                    NotificationType.PAYMENT_DONE,
                    "결제가 완료되었습니다."
            );

        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            tossPaymentClient.cancel(request.paymentKey(), "서버 오류로 인한 자동 취소");  // getPaymentKey() -> paymentKey()
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return PaymentConfirmResponse.of(payment, tossResponse.method(), tossResponse.approvedAt());  // getMethod(), getApprovedAt() -> method(), approvedAt()
    }

}
