package com.back.ovengers.domain.settlement.dto;

import com.back.ovengers.domain.settlement.entity.Settlement;

import java.time.LocalDate;

public record AdminSettlementListResponse(
        Long id,
        Long hostId,
        String hostNickname,
        LocalDate settlementDate,
        int totalAmount,
        int feeAmount,
        int payoutAmount,
        String status
) {
    public static AdminSettlementListResponse of(Settlement settlement) {
        return new AdminSettlementListResponse(
                settlement.getId(),
                settlement.getHost().getId(),
                settlement.getHost().getNickname(),
                settlement.getSettlementDate(),
                settlement.getTotalAmount(),
                settlement.getFeeAmount(),
                settlement.getPayoutAmount(),
                settlement.getStatus().name()
        );
    }
}