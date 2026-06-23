package com.back.ovengers.domain.reservation.dto;

import com.back.ovengers.domain.reservation.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long userId,
        Long siteId,
        String rsvNum,
        String imageUrl,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guestCount,
        Integer rsvPrice,
        String status,
        LocalDateTime createdAt
) {
    public static ReservationResponse of(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getSite().getId(),
                reservation.getRsvNum(),
                reservation.getSite().getCamping().getFirstImageUrl(),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                reservation.getGuestCount(),
                reservation.getRsvPrice(),
                reservation.getStatus().name(),
                reservation.getCreatedAt()
        );
    }
}