package com.back.ovengers.domain.reservation.dto;


import java.time.LocalDate;


public record MyReservationResponse (
        Long id,
        String rsvNum,
        String rsvName,
        String rsvPhone,
        Integer guestCount,
        String request,
        String campingName,
        String siteName,
        String address,
        String imageUrl,
        LocalDate checkIn,
        LocalDate checkOut
){}
