package com.back.ovengers.domain.payment.dto;

import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.reservation.entity.Reservation;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record PaymentConfirmResponse(
        String orderId,
        String rsvNo,
        String campingName,
        String siteName,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer nights,
        Integer guestCount,
        String rsvName,
        String rsvPhone,
        Integer amount,
        String paymentMethod,
        String approvedAt
) {
    public static PaymentConfirmResponse of(Payment payment, String paymentMethod, String approvedAt) {
        Reservation reservation = payment.getReservation();
        return new PaymentConfirmResponse(
                payment.getOrderId(),
                reservation.getRsvNum(),
                reservation.getSite().getCamping().getName(),
                reservation.getSite().getName(),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                (int) ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut()),
                reservation.getGuestCount(),
                reservation.getRsvName(),
                reservation.getRsvPhone(),
                payment.getPaidPrice(),
                paymentMethod,
                approvedAt
        );
    }
}