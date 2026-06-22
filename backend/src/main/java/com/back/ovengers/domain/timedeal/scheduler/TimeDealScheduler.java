package com.back.ovengers.domain.timedeal.scheduler;

import com.back.ovengers.domain.timedeal.repository.TimeDealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TimeDealScheduler {

    private final TimeDealRepository timeDealRepository;

    @Scheduled(fixedRate = 60_000) // 1분마다
    @Transactional
    public void refreshTimeDealStatus() {
        LocalDateTime now = LocalDateTime.now();
        timeDealRepository.activateScheduledDeals(now);
        timeDealRepository.endExpiredDeals(now);
    }
}
