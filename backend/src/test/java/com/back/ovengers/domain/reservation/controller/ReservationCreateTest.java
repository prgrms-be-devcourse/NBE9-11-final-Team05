package com.back.ovengers.domain.reservation.controller;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.dto.ReservationRequest;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.security.JwtProvider;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ReservationCreateTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private CampingRepository campingRepository;
    @Autowired private JwtProvider jwtProvider;

    private User user;
    private Site site;
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
                .name("테스트 캠핑장")
                .region("서울")
                .city("강남구")
                .address("서울시 강남구 테스트로 123")
                .status(CampingStatus.APPROVED)
                .build());

        site = siteRepository.save(Site.builder()
                .camping(camping)
                .name("A구역")
                .baseCapacity(2)
                .maxCapacity(4)
                .totalAmount(5)
                .price(50000)
                .build());

        accessToken = jwtProvider.createRefreshToken(user.getId(), user.getRole().name());
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();
    }

    private ReservationRequest createValidRequest() {
        ReservationRequest request = new ReservationRequest();
        ReflectionTestUtils.setField(request, "siteId", site.getId());
        ReflectionTestUtils.setField(request, "rsvName", "홍길동");
        ReflectionTestUtils.setField(request, "rsvPhone", "010-1234-5678");
        ReflectionTestUtils.setField(request, "checkIn", LocalDate.now().plusDays(1));
        ReflectionTestUtils.setField(request, "checkOut", LocalDate.now().plusDays(3));
        ReflectionTestUtils.setField(request, "guestCount", 2);
        ReflectionTestUtils.setField(request, "request", "늦은 체크인 부탁드립니다.");
        return request;
    }

    @Test
    @DisplayName("예약 생성 성공")
    void createReservation_success() throws Exception {

        ReservationRequest request = createValidRequest();

        mockMvc.perform(post("/api/reservations")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("예약이 생성되었습니다."))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.guestCount").value(2));
    }

    @Test
    @DisplayName("예약 생성 실패 - 구역 없음")
    void createReservation_fail_siteNotFound() throws Exception {

        ReservationRequest request = createValidRequest();
        ReflectionTestUtils.setField(request, "siteId", 999999L);

        mockMvc.perform(post("/api/reservations")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("예약 생성 실패 - 체크인이 체크아웃보다 늦음")
    void createReservation_fail_invalidDate() throws Exception {

        ReservationRequest request = createValidRequest();
        ReflectionTestUtils.setField(request, "checkIn", LocalDate.now().plusDays(5));
        ReflectionTestUtils.setField(request, "checkOut", LocalDate.now().plusDays(3));

        mockMvc.perform(post("/api/reservations")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예약 생성 실패 - 과거 날짜")
    void createReservation_fail_pastDate() throws Exception {

        ReservationRequest request = createValidRequest();
        ReflectionTestUtils.setField(request, "checkIn", LocalDate.now().minusDays(1));
        ReflectionTestUtils.setField(request, "checkOut", LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/reservations")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예약 생성 실패 - 인원수 초과")
    void createReservation_fail_guestCountExceeded() throws Exception {

        ReservationRequest request = createValidRequest();
        ReflectionTestUtils.setField(request, "guestCount", 10); // maxCapacity 4 초과

        mockMvc.perform(post("/api/reservations")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예약 생성 실패 - 필수값 누락")
    void createReservation_fail_missingField() throws Exception {

        ReservationRequest request = new ReservationRequest();
        ReflectionTestUtils.setField(request, "rsvName", "홍길동");
        ReflectionTestUtils.setField(request, "checkIn", LocalDate.now().plusDays(1));
        ReflectionTestUtils.setField(request, "checkOut", LocalDate.now().plusDays(3));

        mockMvc.perform(post("/api/reservations")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}