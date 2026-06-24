package com.back.ovengers.domain.settlement.controller;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.settlement.repository.SettlementDetailRepository;
import com.back.ovengers.domain.settlement.repository.SettlementRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.fixture.SiteFixture;
import com.back.ovengers.fixture.UserFixture;
import com.back.ovengers.global.security.JwtProvider;
import org.springframework.test.util.ReflectionTestUtils;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminSettlementGenerateTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SettlementRepository settlementRepository;
    @Autowired private SettlementDetailRepository settlementDetailRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private CampingRepository campingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtProvider jwtProvider;

    private User admin;
    private String adminToken;
    private Payment payment;
    @BeforeEach
    void setUp() {
        settlementDetailRepository.deleteAll();
        settlementRepository.deleteAll();
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();

        admin = userRepository.save(User.builder()
                .email("admin" + System.nanoTime() + "@test.com")
                .password("password123!")
                .name("관리자")
                .nickname("관리자" + System.nanoTime())
                .phone("010-0000-0000")
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build());

        User host = userRepository.save(
                UserFixture.host()
                        .email("host" + System.nanoTime() + "@test.com")
                        .nickname("호스트" + System.nanoTime())
                        .build()
        );

        User user = userRepository.save(
                UserFixture.user()
                        .email("user" + System.nanoTime() + "@test.com")
                        .nickname("유저" + System.nanoTime())
                        .build()
        );

        Camping camping = campingRepository.save(Camping.builder()
                .host(host)
                .name("테스트 캠핑장")
                .region("서울")
                .city("강남구")
                .address("서울시 강남구 테스트로 123")
                .status(CampingStatus.APPROVED)
                .build());

        Site site = siteRepository.save(SiteFixture.builder()
                .camping(camping)
                .build());

        Reservation reservation = reservationRepository.save(Reservation.builder()
                .user(user)
                .site(site)
                .rsvNum("RSV-" + UUID.randomUUID())
                .rsvName("홍길동")
                .rsvPhone("010-1234-5678")
                .checkIn(LocalDate.of(2026, 6, 15))
                .checkOut(LocalDate.of(2026, 6, 17))
                .guestCount(2)
                .rsvPrice(100000)
                .status(ReservationStatus.CONFIRMED)
                .build());

        payment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ORD-" + UUID.randomUUID())
                .paidPrice(100000)
                .status(PaymentStatus.DONE)
                .build());
        
        // createdAt을 settlementDate 범위 안으로 강제 설정
        ReflectionTestUtils.setField(payment, "createdAt",
                LocalDateTime.of(2026, 6, 17, 12, 0, 0));
        paymentRepository.save(payment);

        adminToken = jwtProvider.createRefreshToken(admin.getId(), admin.getRole().name());
    }

    @AfterEach
    void tearDown() {
        settlementDetailRepository.deleteAll();
        settlementRepository.deleteAll();
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("정산 생성 성공")
    void generateSettlement_success() throws Exception {

        Map<String, String> request = Map.of("settlementDate", "2026-06-21");

        mockMvc.perform(post("/api/admin/settlements/generate")
                        .cookie(new MockCookie("accessToken", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("정산이 생성되었습니다."))
                .andExpect(jsonPath("$.data.generatedCount").value(1))
                .andExpect(jsonPath("$.data.totalPayoutAmount").value(90000));
    }

    @Test
    @DisplayName("정산 생성 실패 - 미래 날짜")
    void generateSettlement_fail_futureDate() throws Exception {

        Map<String, String> request = Map.of("settlementDate", "2099-01-01");

        mockMvc.perform(post("/api/admin/settlements/generate")
                        .cookie(new MockCookie("accessToken", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").value("잘못된 정산 기준일입니다."));
    }

    @Test
    @DisplayName("정산 생성 실패 - 관리자 아닌 경우")
    void generateSettlement_fail_forbidden() throws Exception {

        User host = userRepository.save(
                UserFixture.host()
                        .email("host2" + System.nanoTime() + "@test.com")
                        .nickname("호스트2" + System.nanoTime())
                        .build()
        );

        String hostToken = jwtProvider.createRefreshToken(host.getId(), host.getRole().name());
        Map<String, String> request = Map.of("settlementDate", "2026-06-21");

        mockMvc.perform(post("/api/admin/settlements/generate")
                        .cookie(new MockCookie("accessToken", hostToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}