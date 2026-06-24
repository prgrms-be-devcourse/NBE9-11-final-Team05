package com.back.ovengers.domain.reservation.dto;

import com.back.ovengers.domain.reservation.entity.Reservation;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ReservationResponse(
        Long id,
        Long userId,
        Long siteId,
        String rsvNum,
        String imageUrl,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guestCount,
        Integer rsvPrice,
        String status,
        LocalDateTime createdAt,

        // 타임딜 예약인 경우에만 값이 존재 (일반 예약은 null)
        Long timeDealId,
        Integer dealPrice,
        Integer discountRate
) {
    public static ReservationResponse of(Reservation reservation) {
        ReservationResponseBuilder builder = ReservationResponse.builder()
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
                .createdAt(reservation.getCreatedAt());

        // 타임딜 예약인 경우 추가 정보 포함
        if (reservation.isTimeDealReservation()) {
            int originalPrice = reservation.getTimeDeal().getOriginalPrice();
            int dealPrice = reservation.getTimeDeal().getDealPrice();
            int discountRate = (int) Math.round((1 - (double) dealPrice / originalPrice) * 100);

            builder
                    .timeDealId(reservation.getTimeDeal().getId())
                    .dealPrice(dealPrice)
                    .discountRate(discountRate);
        }

        return builder.build();
    }
}