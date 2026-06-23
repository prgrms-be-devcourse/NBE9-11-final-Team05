package com.back.ovengers.domain.payment.dto;

import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.reservation.entity.Reservation;

import java.time.temporal.ChronoUnit;

public record PaymentResponse(
        String orderId,
        String orderName,
        Integer amount,
        String customerName
) {
    public static PaymentResponse of(Payment payment, Reservation reservation) {
        return new PaymentResponse(
                payment.getOrderId(),
                reservation.getSite().getCamping().getName()
                        + " "
                        + ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut())
                        + "박",
                payment.getPaidPrice(),
                reservation.getRsvName()
        );
    }
}