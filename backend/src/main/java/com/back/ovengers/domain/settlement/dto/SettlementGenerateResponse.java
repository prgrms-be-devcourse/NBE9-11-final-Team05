package com.back.ovengers.domain.settlement.dto;

import java.time.LocalDate;

public record SettlementGenerateResponse(
        int generatedCount,
        LocalDate settlementDate,
        int totalPayoutAmount
) {}