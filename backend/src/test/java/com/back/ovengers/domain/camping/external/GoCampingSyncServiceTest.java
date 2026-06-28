package com.back.ovengers.domain.camping.external;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.fixture.CampingFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoCampingSyncServiceTest {

    @Mock
    private GoCampingClient goCampingClient;

    @Mock
    private CampingRepository campingRepository;

    @Mock
    private GoCampingPersistenceService campingPersistenceService;

    @InjectMocks
    private GoCampingSyncService goCampingSyncService;

    @Test
    @DisplayName("이미지 동기화 성공 시 DONE 상태로 변경된다")
    void success_sync_sets_done() {
        // given
        Camping camp = CampingFixture.builder()
                .contentId(100L)
                .build();

        ReflectionTestUtils.setField(camp, "id", 1L);

        when(campingRepository.findByImageSyncStatusInAndHostIdIsNull(any()))
                .thenReturn(List.of(camp));

        when(goCampingClient.getCampImageList(100L))
                .thenReturn(List.of());

        // when
        goCampingSyncService.syncImageData();

        // then
        verify(campingPersistenceService).saveCampImages(eq(camp), any());
        assert camp.getImageSyncStatus().name().equals("DONE");
    }

    @Test
    @DisplayName("특정 캠핑장에서 예외 발생해도 다음 캠핑장은 계속 처리한다")
    void continue_processing_when_exception_occurs() {
        // given
        Camping camp1 = CampingFixture.builder().contentId(100L).build();
        Camping camp2 = CampingFixture.builder().contentId(200L).build();

        ReflectionTestUtils.setField(camp1, "id", 1L);
        ReflectionTestUtils.setField(camp2, "id", 2L);

        when(campingRepository.findByImageSyncStatusInAndHostIdIsNull(any()))
                .thenReturn(List.of(camp1, camp2));

        when(goCampingClient.getCampImageList(100L))
                .thenThrow(new RuntimeException());

        when(goCampingClient.getCampImageList(200L))
                .thenReturn(List.of());

        // when
        goCampingSyncService.syncImageData();

        // then
        verify(campingPersistenceService).saveCampImages(eq(camp2), any());
    }

    @Test
    @DisplayName("API 호출 제한 시 작업을 중단한다")
    void stop_processing_when_rate_limited() {
        // given
        Camping camp1 = CampingFixture.builder().contentId(100L).build();
        Camping camp2 = CampingFixture.builder().contentId(200L).build();
        Camping camp3 = CampingFixture.builder().contentId(300L).build();

        ReflectionTestUtils.setField(camp1, "id", 1L);
        ReflectionTestUtils.setField(camp2, "id", 2L);
        ReflectionTestUtils.setField(camp3, "id", 3L);

        when(campingRepository.findByImageSyncStatusInAndHostIdIsNull(any()))
                .thenReturn(List.of(camp1, camp2, camp3));

        when(goCampingClient.getCampImageList(100L))
                .thenReturn(List.of());

        when(goCampingClient.getCampImageList(200L))
                .thenThrow(
                        HttpClientErrorException.create(
                                HttpStatus.TOO_MANY_REQUESTS,
                                "",
                                HttpHeaders.EMPTY,
                                null,
                                null
                        )
                );

        // when
        goCampingSyncService.syncImageData();

        // then
        verify(campingPersistenceService, times(1))
                .saveCampImages(eq(camp1), any());

        verify(goCampingClient, never())
                .getCampImageList(300L);
    }
}