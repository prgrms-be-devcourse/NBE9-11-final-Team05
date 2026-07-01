package com.back.ovengers.domain.camping.scheduler;

import com.back.ovengers.domain.camping.external.GoCampingSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GoCampingSchedulerTest {

    @Mock
    private GoCampingSyncService goCampingSyncService;

    @InjectMocks
    private GoCampingScheduler goCampingScheduler;

    @Test
    @DisplayName("스케줄러 실행 시 전날 데이터 동기화 호출")
    void syncModifiedData() {
        goCampingScheduler.syncModifiedData();

        verify(goCampingSyncService).syncModifiedData(any(LocalDate.class));
    }
}
