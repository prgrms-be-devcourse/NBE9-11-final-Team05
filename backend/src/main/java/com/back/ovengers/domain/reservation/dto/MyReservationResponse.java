package com.back.ovengers.domain.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MyReservationResponse {
    private Long id;
    private String rsvNum;
    private String rsvName;
    private String rsvPhone;
    private Integer guestCount;
    private String request;
    private String campingName;
    private String siteName;
    private String address;
    private String imageUrl;
    private LocalDate checkIn;
    private LocalDate checkOut;
}
