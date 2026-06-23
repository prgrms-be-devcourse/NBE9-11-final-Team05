package com.back.ovengers.domain.payment.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull Long reservationId
) {}