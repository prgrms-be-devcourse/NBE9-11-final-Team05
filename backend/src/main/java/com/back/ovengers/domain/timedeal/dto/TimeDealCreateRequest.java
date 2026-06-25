package com.back.ovengers.domain.timedeal.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimeDealCreateRequest(
        @NotNull Long siteId,
        @NotNull @FutureOrPresent LocalDate checkIn,
        @NotNull LocalDate checkOut,
        @NotNull @Min(1) Integer quantity,
        @NotNull @Min(0) Integer dealPrice,
        @NotNull LocalDateTime saleStartAt,
        @NotNull LocalDateTime saleEndAt
) {
}