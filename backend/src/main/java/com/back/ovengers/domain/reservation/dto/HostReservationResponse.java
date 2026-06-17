package com.back.ovengers.domain.reservation.dto;

import com.back.ovengers.domain.reservation.entity.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HostReservationResponse(
    Long id,
    String campingName,
    String siteName,
    String rsvNum,
    String rsvName,
    String rsvPhone,
    LocalDate checkIn,
    LocalDate checkOut,
    int guestCount,
    int rsvPrice,
    ReservationStatus status,
    LocalDateTime createdAt
) {}
