package com.back.ovengers.domain.reservation.controller;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.chat.entity.ChatRoom;
import com.back.ovengers.domain.chat.enums.ChatRoomStatus;
import com.back.ovengers.domain.chat.enums.ChatRoomType;
import com.back.ovengers.domain.chat.repository.ChatRoomRepository;
import com.back.ovengers.domain.payment.client.TossPaymentClient;
import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.reservation.dto.ReservationRequest;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.fixture.UserFixture;
import com.back.ovengers.global.security.JwtProvider;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationRestockTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private CampingRepository campingRepository;
    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private JwtProvider jwtProvider;

    @MockitoBean
    private TossPaymentClient tossPaymentClient;

    private User firstUser;
    private User secondUser;
    private Site site;
    private String firstUserToken;
    private String secondUserToken;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();

        User host = userRepository.save(
                UserFixture.host()
                        .email("host" + System.nanoTime() + "@test.com")
                        .nickname("호스트" + System.nanoTime())
                        .build()
        );

        firstUser = userRepository.save(
                UserFixture.user()
                        .email("first" + System.nanoTime() + "@test.com")
                        .nickname("첫번째유저" + System.nanoTime())
                        .build()
        );

        secondUser = userRepository.save(
                UserFixture.user()
                        .email("second" + System.nanoTime() + "@test.com")
                        .nickname("두번째유저" + System.nanoTime())
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

        // 재고 1개
        site = siteRepository.save(Site.builder()
                .camping(camping)
                .name("A구역")
                .baseCapacity(2)
                .maxCapacity(4)
                .totalAmount(1)
                .price(50000)
                .build());

        firstUserToken = jwtProvider.createRefreshToken(firstUser.getId(), firstUser.getRole().name());
        secondUserToken = jwtProvider.createRefreshToken(secondUser.getId(), secondUser.getRole().name());

        // 토스 취소 API Mock 처리
        doNothing().when(tossPaymentClient).cancel(any(), any());
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();
        chatRoomRepository.deleteAll();
    }

    @Test
    @DisplayName("예약 취소 후 재고 복구 - 다른 사용자가 같은 날짜 예약 가능해야 함")
    void cancelReservation_thenRestock() throws Exception {

        ReservationRequest request = new ReservationRequest(
                site.getId(),
                "홍길동",
                "010-1234-5678",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2,
                null
        );

        String requestBody = objectMapper.writeValueAsString(request);

        // 1. 첫 번째 사용자 예약 생성
        String createResult = mockMvc.perform(post("/api/reservations")
                        .cookie(new MockCookie("accessToken", firstUserToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long reservationId = objectMapper.readTree(createResult)
                .get("data").get("id").asLong();

        // 2. 두 번째 사용자 예약 시도 → 재고 없어서 실패해야 함
        mockMvc.perform(post("/api/reservations")
                        .cookie(new MockCookie("accessToken", secondUserToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());

        // 3. 첫 번째 사용자 예약을 CONFIRMED + DONE 결제로 만들기
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        reservation.updateStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ORD-" + UUID.randomUUID())
                .paymentKey("test_payment_key_" + System.nanoTime())
                .paidPrice(100000)
                .status(PaymentStatus.DONE)
                .build());

        chatRoomRepository.save(
                ChatRoom.builder()
                        .reservationId(reservation.getId())
                        .name("테스트 채팅방")
                        .type(ChatRoomType.DIRECT)
                        .status(ChatRoomStatus.ACTIVE)
                        .build());

        // 4. 첫 번째 사용자 예약 취소
        mockMvc.perform(patch("/api/reservations/{id}/cancel", reservationId)
                        .cookie(new MockCookie("accessToken", firstUserToken)))
                .andExpect(status().isOk());

        // 5. 예약/결제 상태 확인
        Reservation cancelledReservation = reservationRepository.findById(reservationId).orElseThrow();
        assertThat(cancelledReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);

        Payment cancelledPayment = paymentRepository.findAllByReservation_Id(reservationId)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.CANCELLED)
                .findFirst()
                .orElseThrow();
        assertThat(cancelledPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);

        // 6. 두 번째 사용자 재예약 시도 → 재고 복구됐으니 성공해야 함
        mockMvc.perform(post("/api/reservations")
                        .cookie(new MockCookie("accessToken", secondUserToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        // 7. 취소 안 된 예약 1개만 있어야 함
        long activeCount = reservationRepository.findAll()
                .stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .count();
        assertThat(activeCount).isEqualTo(1);
    }
}
