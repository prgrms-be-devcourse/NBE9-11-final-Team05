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
import com.back.ovengers.domain.settlement.entity.Settlement;
import com.back.ovengers.domain.settlement.entity.SettlementDetail;
import com.back.ovengers.domain.settlement.entity.SettlementStatus;
import com.back.ovengers.domain.settlement.repository.SettlementDetailRepository;
import com.back.ovengers.domain.settlement.repository.SettlementRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.fixture.SiteFixture;
import com.back.ovengers.fixture.UserFixture;
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
class SettlementDetailTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private SettlementRepository settlementRepository;
    @Autowired private SettlementDetailRepository settlementDetailRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private CampingRepository campingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtProvider jwtProvider;

    private User host;
    private Settlement settlement;
    private String accessToken;

    @BeforeEach
    void setUp() {
        settlementDetailRepository.deleteAll();
        settlementRepository.deleteAll();
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();

        host = userRepository.save(
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

        Payment payment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ORD-" + UUID.randomUUID())
                .paidPrice(100000)
                .status(PaymentStatus.DONE)
                .build());

        settlement = settlementRepository.save(Settlement.builder()
                .host(host)
                .settlementDate(LocalDate.of(2026, 6, 21))
                .totalAmount(100000)
                .feeAmount(10000)
                .payoutAmount(90000)
                .status(SettlementStatus.PENDING)
                .build());

        settlementDetailRepository.save(SettlementDetail.builder()
                .settlement(settlement)
                .payment(payment)
                .amount(100000)
                .build());

        accessToken = jwtProvider.createRefreshToken(host.getId(), host.getRole().name());
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
    @DisplayName("정산 상세 조회 성공")
    void getSettlementDetail_success() throws Exception {

        mockMvc.perform(get("/api/host/settlements/{id}", settlement.getId())
                        .cookie(new MockCookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("정산 상세 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.id").value(settlement.getId()))
                .andExpect(jsonPath("$.data.totalAmount").value(100000))
                .andExpect(jsonPath("$.data.feeAmount").value(10000))
                .andExpect(jsonPath("$.data.payoutAmount").value(90000))
                .andExpect(jsonPath("$.data.details.length()").value(1))
                .andExpect(jsonPath("$.data.details[0].campingName").value("테스트 캠핑장"))
                .andExpect(jsonPath("$.data.details[0].rsvName").value("홍길동"))
                .andExpect(jsonPath("$.data.details[0].amount").value(100000));
    }

    @Test
    @DisplayName("정산 상세 조회 실패 - 정산 없음")
    void getSettlementDetail_fail_notFound() throws Exception {

        mockMvc.perform(get("/api/host/settlements/{id}", 999999L)
                        .cookie(new MockCookie("accessToken", accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").value("정산 정보를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("정산 상세 조회 실패 - 본인 정산 아님")
    void getSettlementDetail_fail_forbidden() throws Exception {

        User otherHost = userRepository.save(
                UserFixture.host()
                        .email("other" + System.nanoTime() + "@test.com")
                        .nickname("다른호스트" + System.nanoTime())
                        .build()
        );

        String otherToken = jwtProvider.createRefreshToken(otherHost.getId(), otherHost.getRole().name());

        mockMvc.perform(get("/api/host/settlements/{id}", settlement.getId())
                        .cookie(new MockCookie("accessToken", otherToken)))
                .andExpect(status().isForbidden());
    }
}