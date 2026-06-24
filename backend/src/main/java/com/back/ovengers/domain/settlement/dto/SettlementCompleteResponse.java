package com.back.ovengers.domain.settlement.dto;

import com.back.ovengers.domain.settlement.entity.Settlement;

import java.time.LocalDate;

public record SettlementCompleteResponse(
        Long id,
        String hostNickname,
        LocalDate settlementDate,
        int payoutAmount,
        String status
) {
    public static SettlementCompleteResponse of(Settlement settlement) {
        return new SettlementCompleteResponse(
                settlement.getId(),
                settlement.getHost().getNickname(),
                settlement.getSettlementDate(),
                settlement.getPayoutAmount(),
                settlement.getStatus().name()
        );
    }
}