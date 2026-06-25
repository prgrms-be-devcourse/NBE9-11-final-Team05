package com.back.ovengers.domain.timedeal.scheduler;

import com.back.ovengers.domain.timedeal.repository.TimeDealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealScheduler {

    private final TimeDealRepository timeDealRepository;

    /**
     * 타임딜 상태 자동 갱신 (1분 주기)
     *
     * [동시성 안전성]
     * - activateScheduledDeals, endExpiredDeals 모두 벌크 UPDATE
     * - DB 레벨에서 원자적으로 처리되어 별도 락 불필요
     * - 두 UPDATE 사이에 짧은 간격이 있어 SCHEDULED → ENDED 케이스가
     *   일시적으로 ACTIVE를 거치지 않을 수 있으나, 두 번째 UPDATE가
     *   같은 트랜잭션에서 처리되어 결과적으로 정확히 ENDED로 전환됨
     *
     * [다중 인스턴스 주의]
     * 앱 서버를 여러 대 띄우면 각 서버에서 스케줄러가 중복 실행된다.
     * 벌크 UPDATE는 멱등하므로 결과는 동일하지만 불필요한 DB 쓰기가 발생한다.
     * → 운영 환경 다중 인스턴스라면 ShedLock 라이브러리 도입을 권장:
     *   @SchedulerLock(name = "TimeDealScheduler_refresh", lockAtMostFor = "PT55S")
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void refreshTimeDealStatus() {
        LocalDateTime now = LocalDateTime.now();
        int activated = timeDealRepository.activateScheduledDeals(now);
        int ended = timeDealRepository.endExpiredDeals(now);
        log.debug("[TimeDealScheduler] activated={}, ended={}", activated, ended);
    }
}
