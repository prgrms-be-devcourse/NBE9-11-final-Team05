package com.back.ovengers.domain.admin.service;

import com.back.ovengers.domain.admin.dto.*;
import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import com.back.ovengers.global.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminServiceTest {

    @Autowired
    AdminService adminService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CampingRepository campingRepository;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    PaymentRepository paymentRepository;

    User host;

    @BeforeEach
    void setUp() {
        host = userRepository.save(User.builder()
                .email("host@test.com")
                .password("1234")
                .name("호스트")
                .nickname("host")
                .phone("010-1111-2222")
                .role(Role.HOST)
                .status(Status.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("관리자 대시보드를 조회할 수 있다")
    void test1() {
        // given
        User activeUser1 = userRepository.save(User.builder()
                .email("test1@test.com")
                .password("1234")
                .name("홍길동")
                .nickname("길동1")
                .phone("010-1234-5678")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        User activeUser2 = userRepository.save(User.builder()
                .email("test2@test.com")
                .password("1234")
                .name("김철수")
                .nickname("철수")
                .phone("010-1234-5679")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        userRepository.save(User.builder()
                .email("test3@test.com")
                .password("1234")
                .name("정지유저")
                .nickname("정지")
                .phone("010-1234-5680")
                .role(Role.USER)
                .status(Status.BANNED)
                .build());

        campingRepository.save(Camping.builder()
                .name("승인 대기 캠핑장")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.PENDING)
                .build());

        Camping approvedCamping = campingRepository.save(Camping.builder()
                .name("강릉 솔밭 캠핑장")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.APPROVED)
                .build());

        Site site = siteRepository.save(Site.builder()
                .camping(approvedCamping)
                .name("A구역")
                .baseCapacity(2)
                .maxCapacity(4)
                .totalAmount(5)
                .price(50000)
                .build());

        Reservation reservation = reservationRepository.save(Reservation.builder()
                .user(activeUser1)
                .site(site)
                .rsvNum("RV-001")
                .rsvName("홍길동")
                .checkIn(LocalDate.of(2026, 7, 25))
                .checkOut(LocalDate.of(2026, 7, 27))
                .guestCount(4)
                .rsvPrice(100000)
                .status(ReservationStatus.COMPLETED)
                .build());

        paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ORDER-001")
                .paidPrice(100000)
                .status(PaymentStatus.DONE)
                .build());

        // when
        AdminDashboardResponse response = adminService.getDashboard();

        // then
        assertThat(response.totalSalesAmount()).isEqualTo(100000L);
        assertThat(response.activeUserCount()).isEqualTo(3L);
        assertThat(response.pendingCampingCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("결제, 캠핑장이 없으면 0으로 반환된다")
    void test2() {
        // when
        AdminDashboardResponse response = adminService.getDashboard();

        // then
        assertThat(response.totalSalesAmount()).isEqualTo(0L);
        assertThat(response.activeUserCount()).isEqualTo(1L);
        assertThat(response.pendingCampingCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("승인 대기 캠핑장 목록을 조회할 수 있다")
    void test3() {
        // given
        User host = userRepository.save(User.builder()
                .email("host@test.com")
                .password("1234")
                .name("부산멋쟁이")
                .nickname("부산호스트")
                .phone("010-1111-2222")
                .role(Role.HOST)
                .status(Status.ACTIVE)
                .build());

        campingRepository.save(Camping.builder()
                .host(host)
                .name("숲속의 캠핑장")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .businessNum("123-45-67890")
                .status(CampingStatus.PENDING)
                .build());

        campingRepository.save(Camping.builder()
                .host(host)
                .name("승인된 캠핑장")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.APPROVED)
                .build());

        Pageable pageable = PageRequest.of(0, 10);

        // when
        AdminPendingCampingResponse response = adminService.getPendingCampingList(pageable);

        // then
        assertThat(response.pendingCampingCount()).isEqualTo(1L);
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).campingName()).isEqualTo("숲속의 캠핑장");
        assertThat(response.content().get(0).hostName()).isEqualTo("부산멋쟁이");
        assertThat(response.content().get(0).businessNum()).isEqualTo("123-45-67890");
    }

    @Test
    @DisplayName("승인 대기 캠핑장이 없으면 빈 목록을 반환한다")
    void test4() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        AdminPendingCampingResponse response = adminService.getPendingCampingList(pageable);

        // then
        assertThat(response.pendingCampingCount()).isEqualTo(0L);
        assertThat(response.content()).isEmpty();
    }

    @Test
    @DisplayName("캠핑장을 승인할 수 있다")
    void test5() {
        // given
        Camping pendingCamping = campingRepository.save(Camping.builder()
                .host(host)
                .name("승인 대기 캠핑장")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.PENDING)
                .build());

        // when
        adminService.approveCamping(pendingCamping.getId());

        // then
        Camping updated = campingRepository.findById(pendingCamping.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CampingStatus.APPROVED);
    }

    @Test
    @DisplayName("존재하지 않는 캠핑장 승인 시 예외가 발생한다")
    void test6() {
        // when & then
        assertThatThrownBy(() -> adminService.approveCamping(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CAMPING_NOT_FOUND);
    }

    @Test
    @DisplayName("PENDING이 아닌 캠핑장은 승인할 수 없다")
    void test7() {
        // given
        Camping approvedCamping = campingRepository.save(Camping.builder()
                .name("이미 승인된 캠핑장")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.APPROVED)
                .build());

        // when & then
        assertThatThrownBy(() -> adminService.approveCamping(approvedCamping.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CAMPING_NOT_PENDING);
    }

    @Test
    @DisplayName("캠핑장을 거절할 수 있다")
    void test8() {
        // given
        Camping pendingCamping = campingRepository.save(Camping.builder()
                .host(host)
                .name("승인 대기 캠핑장")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.PENDING)
                .build());

        CampingRejectRequest request = new CampingRejectRequest("사업자 정보가 일치하지 않습니다.");

        // when
        adminService.rejectCamping(pendingCamping.getId(), request);

        // then
        Camping updated = campingRepository.findById(pendingCamping.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CampingStatus.REJECTED);
    }

    @Test
    @DisplayName("캠핑장을 일괄 승인할 수 있다")
    void test9() {
        // given
        Camping camping1 = campingRepository.save(Camping.builder()
                .host(host)
                .name("캠핑장1")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.PENDING)
                .build());

        Camping camping2 = campingRepository.save(Camping.builder()
                .host(host)
                .name("캠핑장2")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.PENDING)
                .build());

        CampingBulkApproveRequest request = new CampingBulkApproveRequest(
                List.of(camping1.getId(), camping2.getId())
        );

        // when
        adminService.approveCampingList(request);

        // then
        assertThat(campingRepository.findById(camping1.getId()).orElseThrow().getStatus())
                .isEqualTo(CampingStatus.APPROVED);
        assertThat(campingRepository.findById(camping2.getId()).orElseThrow().getStatus())
                .isEqualTo(CampingStatus.APPROVED);
    }

    @Test
    @DisplayName("존재하지 않는 캠핑장이 포함되면 일괄 승인이 실패한다")
    void test10() {
        // given
        Camping camping1 = campingRepository.save(Camping.builder()
                .name("캠핑장1")
                .region("강원도")
                .city("강릉시")
                .address("강원도 강릉시 ...")
                .status(CampingStatus.PENDING)
                .build());

        CampingBulkApproveRequest request = new CampingBulkApproveRequest(
                List.of(camping1.getId(), 999L)
        );

        // when & then
        assertThatThrownBy(() -> adminService.approveCampingList(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CAMPING_NOT_FOUND);
    }

    @Test
    @DisplayName("회원 목록을 조회할 수 있다")
    void test11() {
        // given
        userRepository.save(User.builder()
                .email("user1@test.com")
                .password("1234")
                .name("홍길동")
                .nickname("길동")
                .phone("010-1111-1111")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        Pageable pageable = PageRequest.of(0, 10);

        // when
        PageResponse<AdminUserResponse> result = adminService.getUsers(null, null, false, null, pageable);

        // then
        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("role 필터로 회원 목록을 조회할 수 있다")
    void test12() {
        // given
        userRepository.save(User.builder()
                .email("user1@test.com")
                .password("1234")
                .name("일반유저")
                .nickname("user1")
                .phone("010-1111-1111")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        Pageable pageable = PageRequest.of(0, 10);

        // when
        PageResponse<AdminUserResponse> result = adminService.getUsers(Role.HOST, null, false, null, pageable);

        // then
        assertThat(result.getContent()).allMatch(u -> u.role().equals("HOST"));
    }

    @Test
    @DisplayName("키워드로 회원을 검색할 수 있다")
    void test13() {
        // given
        userRepository.save(User.builder()
                .email("hong@test.com")
                .password("1234")
                .name("홍길동")
                .nickname("hong")
                .phone("010-1111-1111")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        Pageable pageable = PageRequest.of(0, 10);

        // when
        PageResponse<AdminUserResponse> result = adminService.getUsers(null, null, false, "홍길동", pageable);

        // then
        assertThat(result.getContent()).allMatch(u -> u.name().equals("홍길동"));
    }

    @Test
    @DisplayName("탈퇴 회원을 포함해서 조회할 수 있다")
    void test14() {
        // given
        User deletedUser = userRepository.save(User.builder()
                .email("deleted@test.com")
                .password("1234")
                .name("탈퇴유저")
                .nickname("deleted")
                .phone("010-2222-2222")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());
        deletedUser.delete(); // deletedAt 세팅

        Pageable pageable = PageRequest.of(0, 10);

        // when
        PageResponse<AdminUserResponse> result = adminService.getUsers(null, null, true, null, pageable);

        // then
        assertThat(result.getContent()).anyMatch(AdminUserResponse::isDeleted);
    }

    @Test
    @DisplayName("회원을 정지할 수 있다")
    void test15() {
        // given
        User targetUser = userRepository.save(User.builder()
                .email("user@test.com")
                .password("1234")
                .name("일반유저")
                .nickname("user")
                .phone("010-3333-3333")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        // when
        adminService.banUser(targetUser.getId());

        // then
        User updated = userRepository.findById(targetUser.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Status.BANNED);
    }

    @Test
    @DisplayName("존재하지 않는 회원 정지 시 예외가 발생한다")
    void test16() {
        // when & then
        assertThatThrownBy(() -> adminService.banUser(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("회원 정지를 해제할 수 있다")
    void test17() {
        // given
        User bannedUser = userRepository.save(User.builder()
                .email("banned@test.com")
                .password("1234")
                .name("정지유저")
                .nickname("banned")
                .phone("010-4444-4444")
                .role(Role.USER)
                .status(Status.BANNED)
                .build());

        // when
        adminService.unbanUser(bannedUser.getId());

        // then
        User updated = userRepository.findById(bannedUser.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Status.ACTIVE);
    }
}