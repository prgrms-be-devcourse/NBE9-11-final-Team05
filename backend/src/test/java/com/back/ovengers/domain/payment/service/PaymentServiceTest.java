package com.back.ovengers.domain.payment.service;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.payment.dto.PaymentRequest;
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
import com.back.ovengers.global.security.JwtProvider;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PaymentServiceTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private CampingRepository campingRepository;
    @Autowired private JwtProvider jwtProvider;

    private User user;
    private Reservation reservation;
    private String accessToken;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("test@test.com")
                .password("password123!")
                .name("이형진")
                .nickname("브로진")
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
                .rsvNum("RSV-" + UUID.randomUUID())
                .rsvName("홍길동")
                .rsvPhone("010-1234-5678")
                .checkIn(LocalDate.of(2026, 7, 1))
                .checkOut(LocalDate.of(2026, 7, 3))
                .guestCount(2)
                .rsvPrice(100000)
                .status(ReservationStatus.PENDING)
                .build());

        accessToken = jwtProvider.createRefreshToken(user.getId(), user.getRole().name());
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("결제 생성 성공")
    void createPayment_success() throws Exception {
        PaymentRequest request = new PaymentRequest();
        ReflectionTestUtils.setField(request, "reservationId", reservation.getId());

        mockMvc.perform(post("/api/payments")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.amount").value(100000))
                .andExpect(jsonPath("$.data.customerName").value("홍길동"));
    }

    @Test
    @DisplayName("결제 생성 실패 - 예약 없음")
    void createPayment_fail_reservationNotFound() throws Exception {
        PaymentRequest request = new PaymentRequest();
        ReflectionTestUtils.setField(request, "reservationId", 999L);

        mockMvc.perform(post("/api/payments")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").value("예약을 찾을 수 없습니다."));  // data 필드로 변경
    }

    @Test
    @DisplayName("결제 생성 실패 - 본인 예약 아님")
    void createPayment_fail_forbidden() throws Exception {
        User otherUser = userRepository.save(User.builder()
                .email("other@test.com")
                .password("password123!")
                .name("김철수")
                .nickname("다른유저")
                .phone("010-9999-9999")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        String otherToken = jwtProvider.createRefreshToken(otherUser.getId(), otherUser.getRole().name());

        PaymentRequest request = new PaymentRequest();
        ReflectionTestUtils.setField(request, "reservationId", reservation.getId());

        mockMvc.perform(post("/api/payments")
                        .cookie(new MockCookie("accessToken", otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data").value("본인 예약만 결제 가능합니다."));  // data 필드로 변경
    }

    @Test
    @DisplayName("결제 생성 실패 - 이미 결제된 예약")
    void createPayment_fail_alreadyPaid() throws Exception {
        paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ORD-" + UUID.randomUUID())
                .paidPrice(100000)
                .status(PaymentStatus.DONE)
                .build());

        PaymentRequest request = new PaymentRequest();
        ReflectionTestUtils.setField(request, "reservationId", reservation.getId());

        mockMvc.perform(post("/api/payments")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data").value("이미 결제된 예약입니다."));  // data 필드로 변경
    }

    @Test
    @DisplayName("결제 생성 실패 - 결제 불가 예약 상태")
    void createPayment_fail_invalidReservationStatus() throws Exception {
        reservation.updateStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        PaymentRequest request = new PaymentRequest();
        ReflectionTestUtils.setField(request, "reservationId", reservation.getId());

        mockMvc.perform(post("/api/payments")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").value("결제 가능한 예약 상태가 아닙니다."));  // data 필드로 변경
    }

    @Test
    @DisplayName("결제 생성 성공 - 이전 취소 이력이 있어도 새 결제 가능")
    void createPayment_success_withCanceledHistory() throws Exception {
        // 이전에 취소된 결제 이력
        paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ORD-" + UUID.randomUUID())
                .paidPrice(100000)
                .status(PaymentStatus.CANCELLED)
                .build());

        PaymentRequest request = new PaymentRequest();
        ReflectionTestUtils.setField(request, "reservationId", reservation.getId());

        mockMvc.perform(post("/api/payments")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.amount").value(100000));

        // 취소 이력이 삭제되지 않고 그대로 남아있는지 확인
        long count = paymentRepository.findAllByReservation_Id(reservation.getId()).size();
        org.junit.jupiter.api.Assertions.assertEquals(2, count); // 취소 1건 + 새 결제 1건
    }
}