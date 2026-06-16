package com.back.ovengers.domain.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PaymentRequest {

    @NotNull
    private Long reservationId;
}
