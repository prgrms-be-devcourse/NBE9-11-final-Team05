package com.back.ovengers.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TimeDealReservationRequest(
        @NotBlank String rsvName,
        @NotBlank String rsvPhone,
        @NotNull @Positive Integer guestCount,
        String request
) {
}
