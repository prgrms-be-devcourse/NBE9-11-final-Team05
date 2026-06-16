package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.CampingListResponse;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampingServiceTest {

    @Mock
    private CampingRepository campingRepository;

    @InjectMocks
    private CampingService campingService;

    @Test
    @DisplayName("검색 정상 동작")
    void searchSuccess() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Camping camping = Camping.builder()
                .name("속초해변캠핑장")
                .region("강원도")
                .build();

        Page<Camping> page = new PageImpl<>(List.of(camping));

        when(campingRepository.searchCamping(anyString(), any(Pageable.class))).thenReturn(page);

        // when
        Page<CampingListResponse> result =
                campingService.getCamps("속초", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name())
                .isEqualTo("속초해변캠핑장");
    }


    @Test
    @DisplayName("없는 검색어인 경우 - 결과 없음")
    void searchNoResult() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Page<Camping> emptyPage = new PageImpl<>(List.of());

        when(campingRepository.searchCamping(anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // when
        Page<CampingListResponse> result =
                campingService.getCamps("존재하지 않는 키워드", pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("공백 입력 시 전체 조회 처리")
    void searchBlankKeyword() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Camping camping = Camping.builder()
                .name("속초해변캠핑장")
                .region("강원도")
                .build();

        Page<Camping> page = new PageImpl<>(List.of(camping));

        when(campingRepository.searchCamping(null, pageable))
                .thenReturn(page);

        // when
        Page<CampingListResponse> result =
                campingService.getCamps(" ", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("keyword null이면 전체 조회 처리")
    void searchNullKeyword() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Camping camping = Camping.builder()
                .name("속초해변캠핑장")
                .region("강원도")
                .build();

        Page<Camping> page = new PageImpl<>(List.of(camping));

        when(campingRepository.searchCamping(null, pageable))
                .thenReturn(page);

        // when
        Page<CampingListResponse> result =
                campingService.getCamps(null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }



}
