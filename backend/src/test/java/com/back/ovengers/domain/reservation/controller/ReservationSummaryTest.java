package com.back.ovengers.domain.reservation.controller;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReservationSummaryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private CampingRepository campingRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private User user;
    private Reservation reservation;
    private String accessToken;

    @BeforeEach
    void setUp() {

        user = userRepository.save(User.builder()
                .email("test" + System.nanoTime() + "@test.com")
                .password("password123!")
                .name("홍길동")
                .nickname("테스터" + System.nanoTime())
                .phone("010-1234-5678")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        Camping camping = campingRepository.save(Camping.builder()
                .name("숲속 멋쟁이 캠핑장")
                .region("서울")
                .city("강남구")
                .address("서울시 강남구 테스트로 123")
                .firstImageUrl("https://s3.amazonaws.com/test.jpg")
                .status(CampingStatus.APPROVED)
                .build());

        Site site = siteRepository.save(Site.builder()
                .camping(camping)
                .name("A구역")
                .baseCapacity(2)
                .maxCapacity(4)
                .totalAmount(5)
                .price(50000)
                .build());

        reservation = reservationRepository.save(Reservation.builder()
                .user(user)
                .site(site)
                .rsvNum("RSV-20260101-000001")
                .rsvName("홍길동")
                .rsvPhone("010-1234-5678")
                .checkIn(LocalDate.of(2026, 7, 1))
                .checkOut(LocalDate.of(2026, 7, 3))
                .guestCount(2)
                .rsvPrice(100000)
                .status(ReservationStatus.CONFIRMED)
                .build());

        accessToken = jwtProvider.createAccessToken(user.getId(),user.getRole().name());
    }

    @Test
    @DisplayName("결제 완료 조회 성공")
    void getSummary_success() throws Exception {

        mockMvc.perform(get("/api/reservations/{id}/summary", reservation.getId())
                        .cookie(new MockCookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("결제 완료 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.rsvNum").value("RSV-20260101-000001"))
                .andExpect(jsonPath("$.data.campingName").value("숲속 멋쟁이 캠핑장"))
                .andExpect(jsonPath("$.data.siteName").value("A구역"))
                .andExpect(jsonPath("$.data.guestCount").value(2));
    }

    @Test
    @DisplayName("결제 완료 조회 실패 - 예약 없음")
    void getSummary_fail_reservationNotFound() throws Exception {

        mockMvc.perform(get("/api/reservations/{id}/summary", 999999L)
                        .cookie(new MockCookie("accessToken", accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").value("예약을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("결제 완료 조회 실패 - 본인 예약 아님")
    void getSummary_fail_forbidden() throws Exception {

        User otherUser = userRepository.save(User.builder()
                .email("other" + System.nanoTime() + "@test.com")
                .password("password123!")
                .name("김철수")
                .nickname("다른유저" + System.nanoTime())
                .phone("010-9999-9999")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        String otherToken = jwtProvider.createAccessToken(otherUser.getId(),user.getRole().name());

        mockMvc.perform(get("/api/reservations/{id}/summary", reservation.getId())
                        .cookie(new MockCookie("accessToken", otherToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data").value("접근 권한이 없습니다."));
    }
}