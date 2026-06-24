package com.back.ovengers.domain.settlement.scheduler;

import com.back.ovengers.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final SettlementService settlementService;

    @Scheduled(cron = "0 0 0 * * MON") // 매주 월요일 자정 실행
    public void generateWeeklySettlement() {

        // 지난 주 일요일 (정산 기준 마지막 날)
        LocalDate settlementDate = LocalDate.now().minusDays(1);

        log.info("정산 스케줄러 시작 - settlementDate: {}", settlementDate);

        try {
            var result = settlementService.generate(settlementDate);
            log.info("정산 스케줄러 완료 - 생성 건수: {}, 총 지급액: {}",
                    result.generatedCount(), result.totalPayoutAmount());
        } catch (Exception e) {
            log.error("정산 스케줄러 실패 - {}", e.getMessage());
        }
    }
}