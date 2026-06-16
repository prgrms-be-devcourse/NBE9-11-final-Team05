package com.back.ovengers.domain.camping.service;

import com.back.ovengers.domain.camping.dto.CampingCreateRequest;
import com.back.ovengers.domain.camping.dto.CampingCreateResponse;
import com.back.ovengers.domain.camping.dto.HostCampingListResponse;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
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
}