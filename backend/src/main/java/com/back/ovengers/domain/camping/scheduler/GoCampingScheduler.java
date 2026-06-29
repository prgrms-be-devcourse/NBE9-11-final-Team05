package com.back.ovengers.domain.camping.scheduler;

import com.back.ovengers.domain.camping.external.GoCampingSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoCampingScheduler {

    private final GoCampingSyncService goCampingSyncService;

    @Scheduled(cron = "0 0 10 * * *")
    public void syncModifiedData() {
        LocalDate syncDate = LocalDate.now().minusDays(1);

        log.info("GoCamping {} 변경분 동기화 시작", syncDate);

        goCampingSyncService.syncModifiedData(syncDate);

        log.info("GoCamping {} 변경분 동기화 종료", syncDate);
    }
}
