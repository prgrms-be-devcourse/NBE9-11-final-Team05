package com.back.ovengers.domain.timedeal.dto;

import com.back.ovengers.domain.timedeal.entity.TimeDeal;
import com.back.ovengers.domain.timedeal.entity.TimeDealStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimeDealResponse(
        Long id,
        Long siteId,
        String siteName,
        Long campingId,
        String campingName,
        LocalDate checkIn,
        LocalDate checkOut,
        int quantity,
        int soldCount,
        int remaining,
        int originalPrice,
        int dealPrice,
        int discountRate,
        LocalDateTime saleStartAt,
        LocalDateTime saleEndAt,
        TimeDealStatus status
) {
    public static TimeDealResponse from(TimeDeal td) {
        int discountRate = (int) Math.round(
                (1 - (double) td.getDealPrice() / td.getOriginalPrice()) * 100
        );
        return new TimeDealResponse(
                td.getId(), td.getSite().getId(), td.getSite().getName(),
                td.getSite().getCamping().getId(), td.getSite().getCamping().getName(),
                td.getCheckIn(), td.getCheckOut(),
                td.getQuantity(), td.getSoldCount(), td.getQuantity() - td.getSoldCount(),
                td.getOriginalPrice(), td.getDealPrice(), discountRate,
                td.getSaleStartAt(), td.getSaleEndAt(), td.getStatus()
        );
    }
}
