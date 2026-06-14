package com.back.ovengers.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.antlr.v4.runtime.misc.NotNull;

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
    private Integer guestCount;

    private String request;
}