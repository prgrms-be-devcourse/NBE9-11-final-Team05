package com.back.ovengers.domain.reservation.dto;

import com.back.ovengers.domain.reservation.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationDetailResponse {
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
    private Integer rsvPrice;
    private String status;
    private LocalDateTime createdAt;

    public static ReservationDetailResponse of(Reservation reservation) {
        return ReservationDetailResponse.builder()
                .id(reservation.getId())
                .rsvNum(reservation.getRsvNum())
                .rsvName(reservation.getRsvName())
                .rsvPhone(reservation.getRsvPhone())
                .guestCount(reservation.getGuestCount())
                .request(reservation.getRequest())
                .campingName(reservation.getSite().getCamping().getName())
                .siteName(reservation.getSite().getName())
                .address(reservation.getSite().getCamping().getAddress())
                .imageUrl(reservation.getSite().getCamping().getFirstImageUrl())
                .checkIn(reservation.getCheckIn())
                .checkOut(reservation.getCheckOut())
                .rsvPrice(reservation.getRsvPrice())
                .status(reservation.getStatus().name())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}