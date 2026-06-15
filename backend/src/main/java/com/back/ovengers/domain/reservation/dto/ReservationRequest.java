package com.back.ovengers.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;


import java.time.LocalDate;

@Getter
public class ReservationRequest {

    @NotNull
    private Long siteId;

    @NotBlank
    private String rsvName;

    @NotBlank
    private String rsvPhone;

    @NotNull
    private LocalDate checkIn;

    @NotNull
    private LocalDate checkOut;

    @NotNull
    @Positive
    private Integer guestCount;

    private String request;
}