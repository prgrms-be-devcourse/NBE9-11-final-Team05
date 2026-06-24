package com.back.ovengers.domain.reservation.dto;

import com.back.ovengers.domain.reservation.entity.Reservation;

public record ReservationCancelResponse(
        Long id,
        String rsvNum,
        String status
) {
    public static ReservationCancelResponse of(Reservation reservation) {
        return new ReservationCancelResponse(
                reservation.getId(),
                reservation.getRsvNum(),
                reservation.getStatus().name()
        );
    }
}
