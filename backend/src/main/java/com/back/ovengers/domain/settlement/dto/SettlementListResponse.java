package com.back.ovengers.domain.settlement.dto;

import com.back.ovengers.domain.settlement.entity.Settlement;
import java.time.LocalDate;

public record SettlementListResponse(
        Long id,
        LocalDate settlementDate,
        Integer totalAmount,
        Integer feeAmount,
        Integer payoutAmount,
        String status
) {
    public static SettlementListResponse of(Settlement settlement) {
        return new SettlementListResponse(
                settlement.getId(),
                settlement.getSettlementDate(),
                settlement.getTotalAmount(),
                settlement.getFeeAmount(),
                settlement.getPayoutAmount(),
                settlement.getStatus().name()
        );
    }
}