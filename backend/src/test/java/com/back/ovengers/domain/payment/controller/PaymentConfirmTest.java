package com.back.ovengers.domain.payment.controller;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.payment.dto.PaymentConfirmRequest;
import com.back.ovengers.domain.payment.dto.TossConfirmResponse;
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
import com.back.ovengers.domain.payment.client.TossPaymentClient;
import com.back.ovengers.global.security.JwtProvider;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentConfirmTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private CampingRepository campingRepository;

    @MockitoBean
    private TossPaymentClient tossPaymentClient;

    private Reservation reservation;

    @Autowired
    private JwtProvider jwtProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {

        User user = userRepository.save(User.builder()
                .email("test@test.com")
                .password("password123!")
                .name("홍길동")
                .nickname("테스터")
                .phone("010-1234-5678")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build());

        accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());

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
                .rsvNum("ABC-20260615-A1B2C3D4")
                .rsvName("홍길동")
                .rsvPhone("010-1234-5678")
                .checkIn(LocalDate.of(2026, 7, 1))
                .checkOut(LocalDate.of(2026, 7, 3))
                .guestCount(2)
                .rsvPrice(100000)
                .status(ReservationStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("결제 승인 성공")
    void confirmPayment_success() throws Exception {

        Payment payment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ABC-20260615-A1B2C3D4")
                .paidPrice(100000)
                .status(PaymentStatus.READY)
                .build());

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                "test_payment_key",
                payment.getOrderId(),
                100000
        );

        TossConfirmResponse tossResponse = new TossConfirmResponse(
                "test_payment_key",
                payment.getOrderId(),
                "카드",
                100000,
                "2026-06-15T15:00:00+09:00",
                "DONE"
        );

        given(tossPaymentClient.confirm(any(), any(), any())).willReturn(tossResponse);

        mockMvc.perform(post("/api/payments/confirm")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("결제가 완료되었습니다."))
                .andExpect(jsonPath("$.data.orderId").value(payment.getOrderId()))
                .andExpect(jsonPath("$.data.amount").value(100000));
    }

    @Test
    @DisplayName("결제 승인 실패 - 결제 정보 없음")
    void confirmPayment_fail_paymentNotFound() throws Exception {

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                "test_payment_key",
                "NOT-EXIST-ORDER-ID",
                100000
        );

        mockMvc.perform(post("/api/payments/confirm")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").value("결제 정보를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("결제 승인 실패 - 이미 완료된 결제")
    void confirmPayment_fail_alreadyPaid() throws Exception {

        Payment payment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ABC-20260615-A1B2C3D4")
                .paidPrice(100000)
                .status(PaymentStatus.DONE)
                .build());

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                "test_payment_key",
                payment.getOrderId(),
                100000
        );

        mockMvc.perform(post("/api/payments/confirm")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data").value("이미 결제된 예약입니다."));
    }

    @Test
    @DisplayName("결제 승인 실패 - 금액 불일치")
    void confirmPayment_fail_amountMismatch() throws Exception {

        Payment payment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ABC-20260615-A1B2C3D4")
                .paidPrice(100000)
                .status(PaymentStatus.READY)
                .build());

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                "test_payment_key",
                payment.getOrderId(),
                999999
        );

        mockMvc.perform(post("/api/payments/confirm")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").value("결제 금액이 일치하지 않습니다."));
    }

    @Test
    @DisplayName("결제 승인 실패 - 토스 승인 실패")
    void confirmPayment_fail_tossConfirmFail() throws Exception {

        Payment payment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ABC-20260615-A1B2C3D4")
                .paidPrice(100000)
                .status(PaymentStatus.READY)
                .build());

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                "invalid_key",
                payment.getOrderId(),
                100000
        );

        given(tossPaymentClient.confirm(any(), any(), any()))
                .willThrow(new CustomException(ErrorCode.TOSS_CONFIRM_FAIL));

        mockMvc.perform(post("/api/payments/confirm")
                        .cookie(new MockCookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.data").value("결제 승인에 실패했습니다."));
    }
}