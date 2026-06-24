package com.back.ovengers.domain.reservation.controller;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.dto.TimeDealReservationRequest;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.timedeal.entity.TimeDeal;
import com.back.ovengers.domain.timedeal.repository.TimeDealRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class TimeDealReservationCreateTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private CampingRepository campingRepository;
    @Autowired private TimeDealRepository timeDealRepository;
    @Autowired private JwtProvider jwtProvider;

    private User user;
    private Site site;
    private TimeDeal timeDeal;
    private String accessToken;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                User.builder()
                        .email("test" + System.nanoTime() + "@test.com")
                        .password("password123!")
                        .name("홍길동")
                        .nickname("테스터" + System.nanoTime())
                        .phone("01012345678")
                        .role(Role.USER)
                        .status(Status.ACTIVE)
                        .build()
        );

        Camping camping = campingRepository.save(
                Camping.builder()
                        .name("테스트 캠핑장")
                        .region("서울")
                        .city("강남구")
                        .address("서울시 강남구 테스트로 123")
                        .status(CampingStatus.APPROVED)
                        .build()
        );

        site = siteRepository.save(
                Site.builder()
                        .camping(camping)
                        .name("A구역")
                        .baseCapacity(2)
                        .maxCapacity(4)
                        .totalAmount(10)
                        .price(100000)
                        .build()
        );

        // saleStartAt 과거, saleEndAt 미래 → calculateInitialStatus()가 ACTIVE 반환
        timeDeal = timeDealRepository.save(
                TimeDeal.builder()
                        .site(site)
                        .checkIn(LocalDate.now().plusDays(10))
                        .checkOut(LocalDate.now().plusDays(12))
                        .quantity(5)
                        .originalPrice(100000)
                        .dealPrice(70000)
                        .saleStartAt(LocalDateTime.now().minusHours(1))
                        .saleEndAt(LocalDateTime.now().plusDays(1))
                        .build()
        );

        accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
    }

    private TimeDealReservationRequest createRequest() {
        return new TimeDealReservationRequest("홍길동", "01012345678", 2, "조용한 자리 부탁드립니다.");
    }

    // ───────────────────────────── 성공 ─────────────────────────────

    @Test
    @DisplayName("타임딜 예약 성공")
    void createTimeDealReservation_success() throws Exception {
        mockMvc.perform(
                        post("/api/reservations/timedeal/{timeDealId}", timeDeal.getId())
                                .cookie(new MockCookie("accessToken", accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest()))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("타임딜 예약이 생성되었습니다."))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.guestCount").value(2))
                .andExpect(jsonPath("$.data.timeDealId").value(timeDeal.getId()))
                .andExpect(jsonPath("$.data.dealPrice").value(70000))
                .andExpect(jsonPath("$.data.discountRate").value(30)); // (1 - 70000/100000) * 100
    }

    // ───────────────────────────── 실패 ─────────────────────────────

    @Test
    @DisplayName("타임딜 예약 실패 - 인증 없음")
    void createTimeDealReservation_fail_unauthorized() throws Exception {
        mockMvc.perform(
                        post("/api/reservations/timedeal/{timeDealId}", timeDeal.getId())
                                // 쿠키 없음
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest()))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("타임딜 예약 실패 - 존재하지 않는 타임딜")
    void createTimeDealReservation_fail_timeDealNotFound() throws Exception {
        mockMvc.perform(
                        post("/api/reservations/timedeal/{timeDealId}", 999999L)
                                .cookie(new MockCookie("accessToken", accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest()))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("타임딜 예약 실패 - 인원수 초과")
    void createTimeDealReservation_fail_guestCountExceeded() throws Exception {
        // site.maxCapacity = 4, 요청은 10명
        TimeDealReservationRequest request =
                new TimeDealReservationRequest("홍길동", "01012345678", 10, null);

        mockMvc.perform(
                        post("/api/reservations/timedeal/{timeDealId}", timeDeal.getId())
                                .cookie(new MockCookie("accessToken", accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("타임딜 예약 실패 - rsvName 빈 문자열")
    void createTimeDealReservation_fail_blankRsvName() throws Exception {
        TimeDealReservationRequest request =
                new TimeDealReservationRequest("", "01012345678", 2, null);

        mockMvc.perform(
                        post("/api/reservations/timedeal/{timeDealId}", timeDeal.getId())
                                .cookie(new MockCookie("accessToken", accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("타임딜 예약 실패 - rsvPhone 빈 문자열")
    void createTimeDealReservation_fail_blankRsvPhone() throws Exception {
        TimeDealReservationRequest request =
                new TimeDealReservationRequest("홍길동", "", 2, null);

        mockMvc.perform(
                        post("/api/reservations/timedeal/{timeDealId}", timeDeal.getId())
                                .cookie(new MockCookie("accessToken", accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("타임딜 예약 실패 - guestCount 누락")
    void createTimeDealReservation_fail_missingGuestCount() throws Exception {
        TimeDealReservationRequest request =
                new TimeDealReservationRequest("홍길동", "01012345678", null, null);

        mockMvc.perform(
                        post("/api/reservations/timedeal/{timeDealId}", timeDeal.getId())
                                .cookie(new MockCookie("accessToken", accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("타임딜 예약 실패 - guestCount 음수")
    void createTimeDealReservation_fail_negativeGuestCount() throws Exception {
        TimeDealReservationRequest request =
                new TimeDealReservationRequest("홍길동", "01012345678", -1, null);

        mockMvc.perform(
                        post("/api/reservations/timedeal/{timeDealId}", timeDeal.getId())
                                .cookie(new MockCookie("accessToken", accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("타임딜 예약 실패 - timeDealId 음수 (@Min(1) 검증)")
    void createTimeDealReservation_fail_invalidTimeDealId() throws Exception {
        mockMvc.perform(
                        post("/api/reservations/timedeal/{timeDealId}", -1L)
                                .cookie(new MockCookie("accessToken", accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest()))
                )
                .andExpect(status().isBadRequest());
    }

    // SOLD_OUT 테스트 - findActiveById는 status를 안 걸러내므로 조회됨 → 서비스에서 400 반환

    @Test
    @DisplayName("타임딜 예약 실패 - SOLD_OUT")
    void createTimeDealReservation_fail_soldOut() throws Exception {
        timeDeal.increaseSoldCount(timeDeal.getQuantity()); // soldCount == quantity → status = SOLD_OUT
        timeDealRepository.saveAndFlush(timeDeal);

        mockMvc.perform(
                        post("/api/reservations/timedeal/{timeDealId}", timeDeal.getId())
                                .cookie(new MockCookie("accessToken", accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest()))
                )
                .andExpect(status().isConflict());
    }
}