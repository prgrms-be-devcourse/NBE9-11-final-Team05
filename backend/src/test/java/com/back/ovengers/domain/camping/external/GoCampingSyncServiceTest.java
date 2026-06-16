package com.back.ovengers.domain.camping.external;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoCampingSyncServiceTest {

    @Mock
    private GoCampingClient goCampingClient;

    @Mock
    private CampingRepository campingRepository;

    @Mock
    private CampingImageRepository campingImageRepository;

    @Mock
    private GoCampingPersistenceService campingPersistenceService;

    @InjectMocks
    private GoCampingSyncService goCampingSyncService;



    @Test
    @DisplayName("이미지 API 호출 실패 - 이미 이미지가 존재하는 경우")
    void imagesAlreadyExist() {
        // given
        Camping camp1 = mock(Camping.class);
        Camping camp2 = mock(Camping.class);

        when(camp1.getId()).thenReturn(1L);
        when(camp2.getId()).thenReturn(2L);
        when(camp2.getContentId()).thenReturn(200L);

        when(campingRepository.findAll()).thenReturn(List.of(camp1, camp2));
        when(campingImageRepository.findCampingIdsWithImages()).thenReturn(Set.of(1L));
        when(goCampingClient.getCampImageList(200L)).thenReturn(List.of());

        // when
        goCampingSyncService.syncImageData();

        // then
        verify(goCampingClient).getCampImageList(200L);
        verify(goCampingClient, times(1))
                .getCampImageList(anyLong());

        verify(campingPersistenceService).saveCampImages(eq(camp2), any());
    }

    @Test
    @DisplayName("이미지 API 호출 실패 - camping.contentId가 null인 경우")
    void contentIdIsNull() {
        // given
        Camping camp = mock(Camping.class);

        when(camp.getId()).thenReturn(1L);
        when(camp.getContentId()).thenReturn(null);

        when(campingRepository.findAll()).thenReturn(List.of(camp));
        when(campingImageRepository.findCampingIdsWithImages()).thenReturn(Set.of());

        // when
        goCampingSyncService.syncImageData();

        // then
        verify(goCampingClient, never()).getCampImageList(anyLong());
        verify(campingPersistenceService, never()).saveCampImages(any(), any());
    }

    @Test
    @DisplayName("특정 캠핑장의 예외가 발생해도 다음 캠핑장은 계속 처리한다")
    void continueProcess() {
        // given
        Camping camp1 = mock(Camping.class);
        Camping camp2 = mock(Camping.class);

        when(camp1.getId()).thenReturn(1L);
        when(camp1.getContentId()).thenReturn(100L);

        when(camp2.getId()).thenReturn(2L);
        when(camp2.getContentId()).thenReturn(200L);

        when(campingRepository.findAll()).thenReturn(List.of(camp1, camp2));
        when(campingImageRepository.findCampingIdsWithImages()).thenReturn(Set.of());

        when(goCampingClient.getCampImageList(100L)).thenThrow(new RuntimeException());
        when(goCampingClient.getCampImageList(200L)).thenReturn(List.of());

        // when
        goCampingSyncService.syncImageData();

        // then
        verify(campingPersistenceService).saveCampImages(eq(camp2), any());
    }

    @Test
    @DisplayName("API 호출 횟수 초과시 작업을 중단한다")
    void stopProcessingWhenApiLimitExceeded() {
        // given
        Camping camp1 = mock(Camping.class);
        Camping camp2 = mock(Camping.class);
        Camping camp3 = mock(Camping.class);

        when(camp1.getId()).thenReturn(1L);
        when(camp1.getContentId()).thenReturn(100L);

        when(camp2.getId()).thenReturn(2L);
        when(camp2.getContentId()).thenReturn(200L);

        when(campingRepository.findAll()).thenReturn(List.of(camp1, camp2, camp3));
        when(campingImageRepository.findCampingIdsWithImages()).thenReturn(Set.of());
        when(goCampingClient.getCampImageList(100L)).thenReturn(List.of());
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
        verify(campingPersistenceService, times(1)).saveCampImages(eq(camp1), any());
    }
}
