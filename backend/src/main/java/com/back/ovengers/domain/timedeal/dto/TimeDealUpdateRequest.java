package com.back.ovengers.domain.timedeal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TimeDealUpdateRequest(
        @NotNull @Min(1) Integer quantity,
        @NotNull @Min(0) Integer dealPrice,
        @NotNull LocalDateTime saleStartAt,
        @NotNull LocalDateTime saleEndAt
) {
}