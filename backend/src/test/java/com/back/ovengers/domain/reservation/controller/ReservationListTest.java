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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReservationListTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private CampingRepository campingRepository;
    @Autowired private JwtProvider jwtProvider;

    private User user;
    private String accessToken;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();

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

        // 예약 15건 생성 (페이징 테스트용)
        for (int i = 0; i < 15; i++) {
            reservationRepository.save(Reservation.builder()
                    .user(user)
                    .site(site)
                    .rsvNum("RSV-" + UUID.randomUUID())
                    .rsvName("홍길동")
                    .rsvPhone("010-1234-5678")
                    .checkIn(LocalDate.of(2026, 7, 1).plusDays(i))
                    .checkOut(LocalDate.of(2026, 7, 3).plusDays(i))
                    .guestCount(2)
                    .rsvPrice(100000)
                    .status(ReservationStatus.CONFIRMED)
                    .build());
        }

        accessToken = jwtProvider.createRefreshToken(user.getId(), user.getRole().name());
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("예약 목록 조회 성공 - 첫 페이지")
    void getMyReservations_success_firstPage() throws Exception {

        mockMvc.perform(get("/api/reservations/me")
                        .param("page", "0")
                        .cookie(new MockCookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("예약 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.content.length()").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(15))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(true));
    }

    @Test
    @DisplayName("예약 목록 조회 성공 - 마지막 페이지")
    void getMyReservations_success_lastPage() throws Exception {

        mockMvc.perform(get("/api/reservations/me")
                        .param("page", "1")
                        .cookie(new MockCookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(5))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("예약 목록 조회 성공 - 예약 없는 경우")
    void getMyReservations_success_empty() throws Exception {

        User otherUser = userRepository.save(User.builder()
                .email("other" + System.nanoTime() + "@test.com")
                .password("password123!")
                .name("김철수")
                .nickname("다른유저" + System.nanoTime())
                .phone("010-9999-9999")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        String otherToken = jwtProvider.createRefreshToken(otherUser.getId(), otherUser.getRole().name());

        mockMvc.perform(get("/api/reservations/me")
                        .param("page", "0")
                        .cookie(new MockCookie("accessToken", otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }
}
