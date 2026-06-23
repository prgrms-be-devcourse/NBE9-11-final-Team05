package com.back.ovengers.fixture;

import com.back.ovengers.domain.settlement.entity.Settlement;
import com.back.ovengers.domain.settlement.entity.SettlementStatus;

import java.time.LocalDate;

public class SettlementFixture {

    public static Settlement.SettlementBuilder builder() {
        return Settlement.builder()
                .host(UserFixture.host().build())
                .settlementDate(LocalDate.of(2026, 6, 21))
                .totalAmount(1000000)
                .feeAmount(100000)
                .payoutAmount(900000)
                .status(SettlementStatus.PENDING);
    }

    public static Settlement.SettlementBuilder completed() {
        return Settlement.builder()
                .host(UserFixture.host().build())
                .settlementDate(LocalDate.of(2026, 6, 14))
                .totalAmount(500000)
                .feeAmount(50000)
                .payoutAmount(450000)
                .status(SettlementStatus.COMPLETED);
    }
}