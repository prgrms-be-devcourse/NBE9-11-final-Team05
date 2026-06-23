package com.back.ovengers.domain.payment.dto;

import com.back.ovengers.domain.reservation.entity.Reservation;

import java.time.LocalDate;

public record PaymentSummaryResponse(
        String imageUrl,
        String campingName,
        String rsvNum,
        String rsvName,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guestCount,
        String siteName
) {
    public static PaymentSummaryResponse of(Reservation reservation) {
        return new PaymentSummaryResponse(
                reservation.getSite().getCamping().getFirstImageUrl(),
                reservation.getSite().getCamping().getName(),
                reservation.getRsvNum(),
                reservation.getRsvName(),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                reservation.getGuestCount(),
                reservation.getSite().getName()
        );
    }
}