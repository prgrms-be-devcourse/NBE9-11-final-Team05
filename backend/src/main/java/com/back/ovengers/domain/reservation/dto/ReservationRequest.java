package com.back.ovengers.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ReservationRequest(
        @NotNull Long siteId,
        @NotBlank String rsvName,
        @NotBlank String rsvPhone,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut,
        @NotNull @Positive Integer guestCount,
        String request
) {}