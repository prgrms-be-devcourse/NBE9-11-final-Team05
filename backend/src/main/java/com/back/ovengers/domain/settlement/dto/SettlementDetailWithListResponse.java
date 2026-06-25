package com.back.ovengers.domain.settlement.dto;

import com.back.ovengers.domain.settlement.entity.Settlement;

import java.time.LocalDate;
import java.util.List;

public record SettlementDetailWithListResponse(
        Long id,
        LocalDate settlementDate,
        Integer totalAmount,
        Integer feeAmount,
        Integer payoutAmount,
        String status,
        List<SettlementDetailResponse> details
) {
    public static SettlementDetailWithListResponse of(
            Settlement settlement,
            List<SettlementDetailResponse> details) {
        return new SettlementDetailWithListResponse(
                settlement.getId(),
                settlement.getSettlementDate(),
                settlement.getTotalAmount(),
                settlement.getFeeAmount(),
                settlement.getPayoutAmount(),
                settlement.getStatus().name(),
                details
        );
    }
}