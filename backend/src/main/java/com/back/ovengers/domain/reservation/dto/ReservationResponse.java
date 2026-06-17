package com.back.ovengers.domain.reservation.dto;

import com.back.ovengers.domain.reservation.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationResponse {
    private Long id;
    private Long userId;
    private Long siteId;
    private String rsvNum;
    private String imageUrl;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guestCount;
    private Integer rsvPrice;
    private String status;
    private LocalDateTime createdAt;

    // summary용으로 추가
    private String campingName;
    private String rsvName;
    private String siteName;

    public static ReservationResponse of(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .siteId(reservation.getSite().getId())
                .rsvNum(reservation.getRsvNum())
                .imageUrl(reservation.getSite().getCamping().getFirstImageUrl())
                .checkIn(reservation.getCheckIn())
                .checkOut(reservation.getCheckOut())
                .guestCount(reservation.getGuestCount())
                .rsvPrice(reservation.getRsvPrice())
                .status(reservation.getStatus().name())
                .createdAt(reservation.getCreatedAt())
                .campingName(reservation.getSite().getCamping().getName())
                .rsvName(reservation.getRsvName())
                .siteName(reservation.getSite().getName())
                .build();
    }
}
