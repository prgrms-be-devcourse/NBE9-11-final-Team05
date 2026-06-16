package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.*;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    void 호스트는_캠핑장을_등록할_수_있다() {
        User host = userRepository.save(
                User.builder()
                        .email("host@test.com")
                        .password("password")
                        .name("호스트")
                        .nickname("host")
                        .phone("01012345678")
                        .role(Role.HOST)
                        .status(Status.ACTIVE)
                        .build()
        );

        CampingCreateRequest request = new CampingCreateRequest(
                null,
                "123-45-67890",
                "가평 캠핑장",
                "경기도",
                "가평군",
                "경기도 가평군 어딘가"
        );

        CampingCreateResponse response =
                hostCampingService.register(host.getId(), request);

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(CampingStatus.PENDING);

        Camping saved = campingRepository.findById(response.id()).orElseThrow();
        assertThat(saved.getHost().getId()).isEqualTo(host.getId());
        assertThat(saved.getName()).isEqualTo("가평 캠핑장");
    }

    @Test
    void 존재하지_않는_유저면_예외가_발생한다() {
        CampingCreateRequest request = new CampingCreateRequest(
                null,
                "123-45-67890",
                "가평 캠핑장",
                "경기도",
                "가평군",
                "경기도 가평군 어딘가"
        );

        CustomException exception = assertThrows(
                CustomException.class,
                () -> hostCampingService.register(999L, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 일반_회원은_캠핑장을_등록할_수_없다() {
        User user = userRepository.save(
                User.builder()
                        .email("user@test.com")
                        .password("password")
                        .name("일반회원")
                        .nickname("user")
                        .phone("01011112222")
                        .role(Role.USER)
                        .status(Status.ACTIVE)
                        .build()
        );

        CampingCreateRequest request = new CampingCreateRequest(
                null,
                "123-45-67890",
                "가평 캠핑장",
                "경기도",
                "가평군",
                "경기도 가평군 어딘가"
        );

        CustomException exception = assertThrows(
                CustomException.class,
                () -> hostCampingService.register(user.getId(), request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.HOST_REQUIRED);
    }

    @Test
    void 탈퇴한_회원은_캠핑장을_등록할_수_없다() {
        User host = userRepository.save(
                User.builder()
                        .email("deleted-host@test.com")
                        .password("password")
                        .name("탈퇴호스트")
                        .nickname("deleted_host")
                        .phone("01012345678")
                        .role(Role.HOST)
                        .status(Status.ACTIVE)
                        .build()
        );
        host.delete();

        CampingCreateRequest request = new CampingCreateRequest(
                null,
                "123-45-67890",
                "가평 캠핑장",
                "경기도",
                "가평군",
                "경기도 가평군 어딘가"
        );

        CustomException exception = assertThrows(
                CustomException.class,
                () -> hostCampingService.register(host.getId(), request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ALREADY_DELETED);
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
}