package com.back.ovengers.domain.payment.dto;

import com.back.ovengers.domain.reservation.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PaymentSummaryResponse {
    private String imageUrl;
    private String campingName;
    private String rsvNum;
    private String rsvName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guestCount;
    private String siteName;

    public static PaymentSummaryResponse of(Reservation reservation) {
        return PaymentSummaryResponse.builder()
                .imageUrl(reservation.getSite().getCamping().getFirstImageUrl())
                .campingName(reservation.getSite().getCamping().getName())
                .rsvNum(reservation.getRsvNum())
                .rsvName(reservation.getRsvName())
                .checkIn(reservation.getCheckIn())
                .checkOut(reservation.getCheckOut())
                .guestCount(reservation.getGuestCount())
                .siteName(reservation.getSite().getName())
                .build();
    }
}