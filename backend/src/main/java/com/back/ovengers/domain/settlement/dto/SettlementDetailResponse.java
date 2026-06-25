package com.back.ovengers.domain.settlement.dto;

import com.back.ovengers.domain.settlement.entity.SettlementDetail;

import java.time.LocalDate;

public record SettlementDetailResponse(
        Long paymentId,
        String campingName,
        String rsvName,
        LocalDate checkIn,
        LocalDate checkOut,
        int amount
) {
    public static SettlementDetailResponse of(SettlementDetail detail) {
        return new SettlementDetailResponse(
                detail.getPayment().getId(),
                detail.getPayment().getReservation().getSite().getCamping().getName(),
                detail.getPayment().getReservation().getRsvName(),
                detail.getPayment().getReservation().getCheckIn(),
                detail.getPayment().getReservation().getCheckOut(),
                detail.getAmount()
        );
    }
}