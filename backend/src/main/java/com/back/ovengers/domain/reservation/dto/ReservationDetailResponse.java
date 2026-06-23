package com.back.ovengers.domain.reservation.dto;

import com.back.ovengers.domain.reservation.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationDetailResponse(
        Long id,
        String rsvNum,
        String rsvName,
        String rsvPhone,
        Integer guestCount,
        String request,
        String campingName,
        String siteName,
        String address,
        String imageUrl,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer rsvPrice,
        String status,
        LocalDateTime createdAt
) {
    public static ReservationDetailResponse of(Reservation reservation) {
        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getRsvNum(),
                reservation.getRsvName(),
                reservation.getRsvPhone(),
                reservation.getGuestCount(),
                reservation.getRequest(),
                reservation.getSite().getCamping().getName(),
                reservation.getSite().getName(),
                reservation.getSite().getCamping().getAddress(),
                reservation.getSite().getCamping().getFirstImageUrl(),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                reservation.getRsvPrice(),
                reservation.getStatus().name(),
                reservation.getCreatedAt()
        );
    }
}