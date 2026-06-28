package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.CampingDetailResponse;
import com.back.ovengers.domain.camping.dto.CampingListResponse;
import com.back.ovengers.domain.camping.dto.CampingSearchResponse;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingImage;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingImageRepository;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.review.repository.ReviewRepository;
import com.back.ovengers.domain.site.dto.SiteResponse;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.fixture.CampingFixture;
import com.back.ovengers.fixture.CampingImageFixture;
import com.back.ovengers.fixture.SiteFixture;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampingServiceTest {

    @Mock
    private CampingRepository campingRepository;

    @Mock
    private CampingImageRepository campingImageRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private CampingService campingService;

    @Test
    @DisplayName("검색 정상 동작")
    void search_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Camping camping = CampingFixture.builder()
                .name("테스트캠핑장")
                .build();

        Page<Camping> page = new PageImpl<>(List.of(camping));

        when(campingRepository.searchApprovedCamping(CampingStatus.APPROVED,"%테스트%", pageable))
                .thenReturn(page);

        // when
        Page<CampingListResponse> result =
                campingService.getCampList("테스트", pageable);

        // then
        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent().getFirst().name())
                .isEqualTo("테스트캠핑장");
    }


    @Test
    @DisplayName("검색 결과가 없으면 빈 페이지를 반환한다")
    void search_no_result() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(campingRepository.searchApprovedCamping(CampingStatus.APPROVED, "%존재하지 않는 키워드%", pageable))
                .thenReturn(Page.empty(pageable));

        // when
        Page<CampingListResponse> result =
                campingService.getCampList("존재하지 않는 키워드", pageable);

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("공백 검색어 입력 시 전체 조회한다")
    void search_blank_keyword() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Camping camping1 = CampingFixture.builder()
                .name("1캠핑장")
                .build();

        Camping camping2 = CampingFixture.builder()
                .name("2캠핑장")
                .build();

        Page<Camping> page = new PageImpl<>(List.of(camping1, camping2));

        when(campingRepository.searchApprovedCamping(CampingStatus.APPROVED,null, pageable))
                .thenReturn(page);

        // when
        Page<CampingListResponse> result =
                campingService.getCampList(" ", pageable);

        // then
        assertThat(result.getContent())
                .extracting(CampingListResponse::name)
                .containsExactly("1캠핑장", "2캠핑장");
    }

    @Test
    @DisplayName("null 검색어 입력 시 전체 조회한다")
    void search_null_keyword() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Camping camping = CampingFixture.builder()
                .name("테스트캠핑장")
                .build();

        Page<Camping> page = new PageImpl<>(List.of(camping));

        when(campingRepository.searchApprovedCamping(CampingStatus.APPROVED, null, pageable))
                .thenReturn(page);

        // when
        Page<CampingListResponse> result =
                campingService.getCampList(null, pageable);

        // then
        assertThat(result.getContent())
                .extracting(CampingListResponse::name)
                .containsExactly("테스트캠핑장");
    }

    @Test
    @DisplayName("캠핑장 상세 조회 성공")
    void get_camp_detail_success() {
        // given
        Long campingId = 1L;

        Camping camping = CampingFixture.builder()
                .name("테스트캠핑장")
                .build();

        CampingImage image1 = CampingImageFixture.builder()
                .camping(camping)
                .imageUrl("image1.jpg")
                .build();

        CampingImage image2 = CampingImageFixture.builder()
                .camping(camping)
                .imageUrl("image2.jpg")
                .build();

        Site site = SiteFixture.builder()
                .camping(camping)
                .name("A구역")
                .build();

        when(campingRepository.findById(campingId))
                .thenReturn(Optional.of(camping));

        when(campingImageRepository.findByCampingId(campingId))
                .thenReturn(List.of(image1, image2));

        when(siteRepository.findByCampingId(campingId))
                .thenReturn(List.of(site));

        // when
        CampingDetailResponse result = campingService.getCampDetail(campingId);

        // then
        assertThat(result.name())
                .isEqualTo("테스트캠핑장");

        assertThat(result.imageUrls())
                .containsExactly("image1.jpg", "image2.jpg");

        assertThat(result.sites())
                .extracting(SiteResponse::name)
                .containsExactly("A구역");
    }

    @Test
    @DisplayName("존재하지 않는 캠핑장 조회 시 예외 발생")
    void get_camp_detail_when_camping_not_found() {
        // given
        Long campingId = 1L;

        when(campingRepository.findById(campingId))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(
                CustomException.class,
                () -> campingService.getCampDetail(campingId)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.CAMPING_NOT_FOUND);
    }


    @Test
    @DisplayName("캠핑장 검색 - 조건 없이 전체 조회")
    void search_available_campings_no_condition() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Camping camping = CampingFixture.builder()
                .name("테스트캠핑장")
                .build();

        Page<Camping> page = new PageImpl<>(List.of(camping));

        when(campingRepository.searchAvailableCampings(
                null, null, null, null, null, null, null, pageable))
                .thenReturn(page);

        when(campingImageRepository.findImagesByCampingIds(anyList()))
                .thenReturn(List.of());

        when(reviewRepository.findRatingStatsByCampingIds(anyList()))
                .thenReturn(List.of());

        when(siteRepository.findMinPriceByCampingIds(anyList()))
                .thenReturn(List.of());

        // when
        Page<CampingSearchResponse> result = campingService.searchAvailableCampings(
                null, null, null, null, null, null, null, pageable
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("테스트캠핑장");
    }

    @Test
    @DisplayName("캠핑장 검색 - 조건 만족하는 캠핑장 없으면 빈 페이지 반환")
    void search_available_campings_empty() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(campingRepository.searchAvailableCampings(
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty(pageable));

        // when
        Page<CampingSearchResponse> result = campingService.searchAvailableCampings(
                null, null, null, null, null, null, null, pageable
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("캠핑장 검색 - 이미지, 별점, 가격 정보 포함")
    void search_available_campings_with_info() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Camping camping = CampingFixture.builder()
                .name("테스트캠핑장")
                .build();
        ReflectionTestUtils.setField(camping, "id", 1L);

        Page<Camping> page = new PageImpl<>(List.of(camping));

        when(campingRepository.searchAvailableCampings(
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        // 이미지 목록
        List<Object[]> imageRows = new ArrayList<>();
        imageRows.add(new Object[]{camping.getId(), "image1.jpg"});
        when(campingImageRepository.findImagesByCampingIds(anyList()))
                .thenReturn(imageRows);

        // 별점, 리뷰 수
        List<Object[]> ratingRows = new ArrayList<>();
        ratingRows.add(new Object[]{camping.getId(), 4.5, 10L});
        when(reviewRepository.findRatingStatsByCampingIds(anyList()))
                .thenReturn(ratingRows);

        // 최저가
        List<Object[]> priceRows = new ArrayList<>();
        priceRows.add(new Object[]{camping.getId(), 50000});
        when(siteRepository.findMinPriceByCampingIds(anyList()))
                .thenReturn(priceRows);

        // when
        Page<CampingSearchResponse> result = campingService.searchAvailableCampings(
                null, null, null, null, null, null, null, pageable
        );

        // then
        CampingSearchResponse response = result.getContent().getFirst();
        assertThat(response.name()).isEqualTo("테스트캠핑장");
        assertThat(response.images()).containsExactly("image1.jpg");
        assertThat(response.averageRating()).isEqualTo(4.5);
        assertThat(response.reviewCount()).isEqualTo(10L);
        assertThat(response.minPrice()).isEqualTo(50000);
    }

}
