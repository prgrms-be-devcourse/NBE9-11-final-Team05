package com.back.ovengers.domain.site.service;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.dto.SiteCreateRequest;
import com.back.ovengers.domain.site.dto.SiteUpdateRequest;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HostSiteServiceTest {

    @Mock private CampingRepository campingRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private SiteRepository siteRepository;

    @InjectMocks
    private HostSiteService hostSiteService;

    User host;
    User otherHost;
    Camping camping;
    Site site;

    @BeforeEach
    void setUp() {
        host = User.builder().email("host@test.com").password("1234").name("호스트")
                .nickname("호스트닉").phone("010-1111-2222").role(Role.HOST).status(Status.ACTIVE).build();
        ReflectionTestUtils.setField(host, "id", 1L);

        otherHost = User.builder().email("other@test.com").password("1234").name("다른호스트")
                .nickname("다른닉").phone("010-3333-4444").role(Role.HOST).status(Status.ACTIVE).build();
        ReflectionTestUtils.setField(otherHost, "id", 2L);

        camping = Camping.builder().host(host).name("테스트캠핑장").region("강원").city("강릉")
                .address("강원도 강릉시").firstImageUrl("https://image.com/test.jpg")
                .rating(4.5f).status(CampingStatus.APPROVED).build();
        ReflectionTestUtils.setField(camping, "id", 1L);

        site = Site.create(camping, new SiteCreateRequest("A구역", "설명", 2, 4, 10, 30000));
        ReflectionTestUtils.setField(site, "id", 1L);
    }

    @Test
    @DisplayName("구역 목록 조회 성공")
    void getSites_success() {
        when(campingRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(camping));
        when(siteRepository.findByCampingIdAndDeletedAtIsNullOrderByIdAsc(1L)).thenReturn(List.of(site));

        var result = hostSiteService.getSites(host.getId(), 1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("구역 목록 조회 실패 - 캠핑장 없음")
    void getSites_fail_campingNotFound() {
        when(campingRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hostSiteService.getSites(host.getId(), 99L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.CAMPING_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("구역 목록 조회 실패 - 다른 호스트의 캠핑장")
    void getSites_fail_notOwner() {
        when(campingRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(camping));

        assertThatThrownBy(() -> hostSiteService.getSites(otherHost.getId(), 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NOT_CAMPING_OWNER.getMessage());
    }

    @Test
    @DisplayName("구역 등록 성공")
    void addSite_success() {
        SiteCreateRequest request = new SiteCreateRequest("B구역", "설명", 2, 4, 5, 30000);

        when(campingRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(camping));
        when(siteRepository.existsByCampingIdAndNameAndDeletedAtIsNull(1L, "B구역")).thenReturn(false);
        when(siteRepository.save(any())).thenReturn(site);

        var result = hostSiteService.addSite(host.getId(), 1L, request);

        assertThat(result).isNotNull();
        verify(siteRepository).save(any());
    }

    @Test
    @DisplayName("구역 등록 실패 - 구역명 중복")
    void addSite_fail_duplicateName() {
        SiteCreateRequest request = new SiteCreateRequest("A구역", "설명", 2, 4, 5, 30000);

        when(campingRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(camping));
        when(siteRepository.existsByCampingIdAndNameAndDeletedAtIsNull(1L, "A구역")).thenReturn(true);

        assertThatThrownBy(() -> hostSiteService.addSite(host.getId(), 1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.DUPLICATE_SITE_NAME.getMessage());

        verify(siteRepository, never()).save(any());
    }

    @Test
    @DisplayName("구역 등록 실패 - 기준 인원 > 최대 인원")
    void addSite_fail_invalidCapacity() {
        SiteCreateRequest request = new SiteCreateRequest("C구역", "설명", 5, 2, 5, 30000);

        when(campingRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(camping));
        when(siteRepository.existsByCampingIdAndNameAndDeletedAtIsNull(1L, "C구역")).thenReturn(false);

        assertThatThrownBy(() -> hostSiteService.addSite(host.getId(), 1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_CAPACITY.getMessage());
    }

    @Test
    @DisplayName("구역 수정 성공")
    void updateSite_success() {
        SiteUpdateRequest request = new SiteUpdateRequest("B구역", null, null, null, null, 50000);

        when(siteRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(site));
        when(siteRepository.existsByCampingIdAndNameAndIdNotAndDeletedAtIsNull(1L, "B구역", 1L)).thenReturn(false);

        var result = hostSiteService.updateSite(host.getId(), 1L, 1L, request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("구역 수정 실패 - 구역명 중복")
    void updateSite_fail_duplicateName() {
        SiteUpdateRequest request = new SiteUpdateRequest("중복구역", null, null, null, null, null);

        when(siteRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(site));
        when(siteRepository.existsByCampingIdAndNameAndIdNotAndDeletedAtIsNull(1L, "중복구역", 1L)).thenReturn(true);

        assertThatThrownBy(() -> hostSiteService.updateSite(host.getId(), 1L, 1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.DUPLICATE_SITE_NAME.getMessage());
    }

    @Test
    @DisplayName("구역 수정 실패 - 기준 인원 > 최대 인원")
    void updateSite_fail_invalidCapacity() {
        SiteUpdateRequest request = new SiteUpdateRequest(null, null, 10, 2, null, null);

        when(siteRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(site));

        assertThatThrownBy(() -> hostSiteService.updateSite(host.getId(), 1L, 1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INVALID_CAPACITY.getMessage());
    }

    @Test
    @DisplayName("구역 삭제 실패 - 확정된 예약 존재")
    void deleteSite_fail_confirmedReservationExists() {
        when(siteRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(site));
        when(reservationRepository.existsBySiteIdAndStatus(1L, ReservationStatus.CONFIRMED)).thenReturn(true);

        assertThatThrownBy(() -> hostSiteService.deleteSite(host.getId(), 1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.CONFIRMED_RESERVATION_EXISTS.getMessage());
    }

    @Test
    @DisplayName("구역 수정 실패 - 다른 호스트의 사이트")
    void updateSite_fail_notOwner() {
        when(siteRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(site));

        assertThatThrownBy(() -> hostSiteService.updateSite(otherHost.getId(), 1L, 1L,
                new SiteUpdateRequest(null, null, null, null, null, 50000)))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NOT_CAMPING_OWNER.getMessage());
    }
}
