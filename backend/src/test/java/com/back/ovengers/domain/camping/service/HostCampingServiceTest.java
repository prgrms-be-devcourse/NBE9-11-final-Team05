package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.*;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.dto.SiteCreateRequest;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HostCampingServiceTest {

    @Autowired
    HostCampingService hostCampingService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CampingRepository campingRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 호스트는_캠핑장과_구역을_등록할_수_있다() {
        User host = createHost("host@test.com", "host");

        CampingCreateRequest request = createCampingCreateRequest();

        CampingCreateResponse response =
                hostCampingService.register(host.getId(), request);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("가평 캠핑장");
        assertThat(response.status()).isEqualTo(CampingStatus.PENDING);

        Camping savedCamping = campingRepository.findById(response.id()).orElseThrow();
        assertThat(savedCamping.getHost().getId()).isEqualTo(host.getId());
        assertThat(savedCamping.getName()).isEqualTo("가평 캠핑장");

        List<Site> savedSites = siteRepository.findAll();

        assertThat(savedSites).hasSize(2);
        assertThat(savedSites)
                .extracting(Site::getName)
                .containsExactlyInAnyOrder("A구역", "B구역");

        assertThat(savedSites)
                .allMatch(site -> site.getCamping().getId().equals(savedCamping.getId()));
    }

    @Test
    void 기준_인원이_최대_인원보다_크면_예외가_발생한다() {
        User host = createHost("invalid-capacity-host@test.com", "invalid_capacity_host");

        CampingCreateRequest request = new CampingCreateRequest(
                null,
                "123-45-67890",
                "가평 캠핑장",
                "경기도",
                "가평군",
                "경기도 가평군 어딘가",
                List.of(
                        new SiteCreateRequest(
                                "A구역",
                                "기준 인원이 최대 인원보다 큰 구역",
                                5,
                                4,
                                10,
                                50000
                        )
                )
        );

        CustomException exception = assertThrows(
                CustomException.class,
                () -> hostCampingService.register(host.getId(), request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CAPACITY);
    }

    private User createHost(String email, String nickname) {
        return userRepository.save(
                User.builder()
                        .email(email)
                        .password("password")
                        .name("호스트")
                        .nickname(nickname)
                        .phone("01012345678")
                        .role(Role.HOST)
                        .status(Status.ACTIVE)
                        .build()
        );
    }

    private CampingCreateRequest createCampingCreateRequest() {
        return new CampingCreateRequest(
                null,
                "123-45-67890",
                "가평 캠핑장",
                "경기도",
                "가평군",
                "경기도 가평군 어딘가",
                List.of(
                        new SiteCreateRequest(
                                "A구역",
                                "기본 오토캠핑 구역",
                                2,
                                4,
                                10,
                                50000
                        ),
                        new SiteCreateRequest(
                                "B구역",
                                "가족 캠핑 구역",
                                4,
                                6,
                                5,
                                80000
                        )
                )
        );
    }

    @Test
    void 내_캠핑장_목록을_조회한다() {
        User host = userRepository.save(
                User.builder()
                        .email("list-host@test.com")
                        .password("password")
                        .name("호스트")
                        .nickname("list_host")
                        .phone("01012345678")
                        .role(Role.HOST)
                        .status(Status.ACTIVE)
                        .build()
        );

        campingRepository.save(
                Camping.builder()
                        .host(host)
                        .businessNum("123-45-67890")
                        .name("가평 캠핑장")
                        .region("경기도")
                        .city("가평군")
                        .address("가평읍 123")
                        .status(CampingStatus.PENDING)
                        .build()
        );

        campingRepository.save(
                Camping.builder()
                        .host(host)
                        .businessNum("111-22-33333")
                        .name("양평 캠핑장")
                        .region("경기도")
                        .city("양평군")
                        .address("양평읍 456")
                        .status(CampingStatus.PENDING)
                        .build()
        );

        List<HostCampingListResponse> result =
                hostCampingService.getMyCampings(host.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(HostCampingListResponse::name)
                .containsExactlyInAnyOrder("가평 캠핑장", "양평 캠핑장");
    }

    @Test
    void 등록된_캠핑장이_없으면_빈_목록을_반환한다() {
        User host = userRepository.save(
                User.builder()
                        .email("empty-host@test.com")
                        .password("password")
                        .name("빈호스트")
                        .nickname("empty_host")
                        .phone("01011112222")
                        .role(Role.HOST)
                        .status(Status.ACTIVE)
                        .build()
        );

        List<HostCampingListResponse> result =
                hostCampingService.getMyCampings(host.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void 호스트는_본인_캠핑장을_수정할_수_있다() {
        User host = userRepository.save(
                User.builder()
                        .email("update-host@test.com")
                        .password("password")
                        .name("수정호스트")
                        .nickname("update_host")
                        .phone("01012345678")
                        .role(Role.HOST)
                        .status(Status.ACTIVE)
                        .build()
        );

        Camping camping = campingRepository.save(
                Camping.builder()
                        .host(host)
                        .businessNum("123-45-67890")
                        .name("기존 캠핑장")
                        .region("경기도")
                        .city("가평군")
                        .address("기존 주소")
                        .description("기존 소개")
                        .status(CampingStatus.PENDING)
                        .build()
        );

        CampingUpdateRequest request = new CampingUpdateRequest(
                "https://cdn.camping.com/images/update-thumb.jpg",
                "강릉 솔밭 캠핑장",
                "https://gangneung-camp.com",
                "강원도",
                "강릉시",
                "강원도 강릉시 연곡면 해안로 123",
                "바다와 소나무 숲을 함께 즐길 수 있는 캠핑장입니다.",
                "033-123-4567",
                LocalTime.of(14, 0),
                LocalTime.of(11, 0),
                "22시 이후 매너타임을 준수해주세요.",
                new BigDecimal("37.7891234"),
                new BigDecimal("128.9123456")
        );

        CampingUpdateResponse response =
                hostCampingService.updateCamping(host.getId(), camping.getId(), request);

        assertThat(response.id()).isEqualTo(camping.getId());
        assertThat(response.name()).isEqualTo("강릉 솔밭 캠핑장");
        assertThat(response.region()).isEqualTo("강원도");
        assertThat(response.city()).isEqualTo("강릉시");
        assertThat(response.address()).isEqualTo("강원도 강릉시 연곡면 해안로 123");
        assertThat(response.description()).isEqualTo("바다와 소나무 숲을 함께 즐길 수 있는 캠핑장입니다.");
        assertThat(response.phone()).isEqualTo("033-123-4567");
        assertThat(response.checkInTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(response.checkOutTime()).isEqualTo(LocalTime.of(11, 0));
    }

    @Test
    void 존재하지_않는_캠핑장이면_수정할_수_없다() {
        User host = userRepository.save(
                User.builder()
                        .email("not-found-host@test.com")
                        .password("password")
                        .name("호스트")
                        .nickname("not_found_host")
                        .phone("01011112222")
                        .role(Role.HOST)
                        .status(Status.ACTIVE)
                        .build()
        );

        CampingUpdateRequest request = new CampingUpdateRequest(
                "https://cdn.camping.com/images/update-thumb.jpg",
                "강릉 솔밭 캠핑장",
                "https://gangneung-camp.com",
                "강원도",
                "강릉시",
                "강원도 강릉시 연곡면 해안로 123",
                "바다와 소나무 숲을 함께 즐길 수 있는 캠핑장입니다.",
                "033-123-4567",
                LocalTime.of(14, 0),
                LocalTime.of(11, 0),
                "22시 이후 매너타임을 준수해주세요.",
                new BigDecimal("37.7891234"),
                new BigDecimal("128.9123456")
        );

        CustomException exception = assertThrows(
                CustomException.class,
                () -> hostCampingService.updateCamping(host.getId(), 999L, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CAMPING_NOT_FOUND);
    }

    @Test
    void 다른_호스트의_캠핑장은_수정할_수_없다() {
        User owner = userRepository.save(
                User.builder()
                        .email("owner@test.com")
                        .password("password")
                        .name("소유자")
                        .nickname("owner")
                        .phone("01022223333")
                        .role(Role.HOST)
                        .status(Status.ACTIVE)
                        .build()
        );

        User anotherHost = userRepository.save(
                User.builder()
                        .email("another-host@test.com")
                        .password("password")
                        .name("다른호스트")
                        .nickname("another_host")
                        .phone("01033334444")
                        .role(Role.HOST)
                        .status(Status.ACTIVE)
                        .build()
        );

        Camping camping = campingRepository.save(
                Camping.builder()
                        .host(owner)
                        .businessNum("123-45-67890")
                        .name("소유자 캠핑장")
                        .region("경기도")
                        .city("가평군")
                        .address("가평 주소")
                        .status(CampingStatus.PENDING)
                        .build()
        );

        CampingUpdateRequest request = new CampingUpdateRequest(
                "https://cdn.camping.com/images/update-thumb.jpg",
                "강릉 솔밭 캠핑장",
                "https://gangneung-camp.com",
                "강원도",
                "강릉시",
                "강원도 강릉시 연곡면 해안로 123",
                "바다와 소나무 숲을 함께 즐길 수 있는 캠핑장입니다.",
                "033-123-4567",
                LocalTime.of(14, 0),
                LocalTime.of(11, 0),
                "22시 이후 매너타임을 준수해주세요.",
                new BigDecimal("37.7891234"),
                new BigDecimal("128.9123456")
        );

        CustomException exception = assertThrows(
                CustomException.class,
                () -> hostCampingService.updateCamping(anotherHost.getId(), camping.getId(), request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_CAMPING_OWNER);
    }

    @Test
    void 호스트는_본인_캠핑장을_삭제할_수_있다() {
        User host = userRepository.save(
                User.builder()
                        .email("delete-host@test.com")
                        .password("password")
                        .name("호스트")
                        .nickname("delete_host")
                        .phone("01012345678")
                        .role(Role.HOST)
                        .status(Status.ACTIVE)
                        .build()
        );

        Camping camping = campingRepository.save(
                Camping.builder()
                        .host(host)
                        .businessNum("123-45-67890")
                        .name("삭제 테스트 캠핑장")
                        .region("경기도")
                        .city("가평군")
                        .address("가평 주소")
                        .status(CampingStatus.PENDING)
                        .build()
        );

        hostCampingService.deleteCamping(host.getId(), camping.getId());

        Camping deletedCamping = campingRepository.findById(camping.getId())
                .orElseThrow();

        assertThat(deletedCamping.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("확정 예약이 존재하면 캠핑장 삭제에 실패한다")
    void deleteCamping_fail_whenConfirmedReservationExists() {
        // given
        User host = userRepository.save(
                User.builder()
                        .email("host@test.com")
                        .password("1234")
                        .name("호스트")
                        .nickname("host")
                        .phone("01012341234")
                        .role(Role.HOST)
                        .status(Status.ACTIVE)
                        .build()
        );

        User user = userRepository.save(
                User.builder()
                        .email("user@test.com")
                        .password("1234")
                        .name("유저")
                        .nickname("user")
                        .phone("01056785678")
                        .role(Role.USER)
                        .status(Status.ACTIVE)
                        .build()
        );

        Camping camping = campingRepository.save(
                Camping.builder()
                        .host(host)
                        .name("테스트 캠핑장")
                        .region("경기")
                        .city("가평")
                        .address("가평군")
                        .status(CampingStatus.APPROVED)
                        .build()
        );

        Site site = siteRepository.save(
                Site.builder()
                        .camping(camping)
                        .name("A구역")
                        .description("테스트 사이트")
                        .baseCapacity(2)
                        .maxCapacity(4)
                        .totalAmount(10)
                        .price(50000)
                        .build()
        );

        reservationRepository.save(
                Reservation.builder()
                        .user(user)
                        .site(site)
                        .rsvNum("RSV-001")
                        .rsvName("예약자")
                        .rsvPhone("01099998888")
                        .checkIn(LocalDate.now().plusDays(1))
                        .checkOut(LocalDate.now().plusDays(2))
                        .guestCount(2)
                        .rsvPrice(50000)
                        .request("조용한 자리 부탁드립니다.")
                        .status(ReservationStatus.CONFIRMED)
                        .build()
        );

        // when & then
        CustomException exception = assertThrows(
                CustomException.class,
                () -> hostCampingService.deleteCamping(host.getId(), camping.getId())
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.CONFIRMED_RESERVATION_EXISTS);

        Camping foundCamping = campingRepository.findById(camping.getId())
                .orElseThrow();

        assertThat(foundCamping.getDeletedAt()).isNull();
    }
}