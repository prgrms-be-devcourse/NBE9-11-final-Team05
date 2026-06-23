package com.back.ovengers.domain.settlement.scheduler;

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
import com.back.ovengers.domain.settlement.entity.SettlementStatus;
import com.back.ovengers.domain.settlement.repository.SettlementDetailRepository;
import com.back.ovengers.domain.settlement.repository.SettlementRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.fixture.SiteFixture;
import com.back.ovengers.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SettlementSchedulerTest {

    @Autowired private SettlementScheduler settlementScheduler;
    @Autowired private SettlementRepository settlementRepository;
    @Autowired private SettlementDetailRepository settlementDetailRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private CampingRepository campingRepository;
    @Autowired private UserRepository userRepository;

    private User host;
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

        payment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ORD-" + UUID.randomUUID())
                .paidPrice(100000)
                .status(PaymentStatus.DONE)
                .build());

        // createdAt을 지난 주 날짜로 강제 설정
        ReflectionTestUtils.setField(payment, "createdAt",
                LocalDateTime.of(2026, 6, 17, 12, 0, 0));
        paymentRepository.save(payment);
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
    @DisplayName("정산 스케줄러 실행 성공 - Settlement 생성됨")
    void generateWeeklySettlement_success() {

        settlementScheduler.generateWeeklySettlement();

        List<Settlement> settlements = settlementRepository.findAll();
        assertThat(settlements).hasSize(1);
        assertThat(settlements.get(0).getHost().getId()).isEqualTo(host.getId());
        assertThat(settlements.get(0).getTotalAmount()).isEqualTo(100000);
        assertThat(settlements.get(0).getFeeAmount()).isEqualTo(10000);
        assertThat(settlements.get(0).getPayoutAmount()).isEqualTo(90000);
        assertThat(settlements.get(0).getStatus()).isEqualTo(SettlementStatus.PENDING);
    }

    @Test
    @DisplayName("정산 스케줄러 실행 성공 - 이미 정산된 Payment는 제외됨")
    void generateWeeklySettlement_excludeAlreadySettled() {

        // 1차 스케줄러 실행
        settlementScheduler.generateWeeklySettlement();

        // 2차 스케줄러 실행 (같은 Payment가 다시 포함되면 안 됨)
        settlementScheduler.generateWeeklySettlement();

        List<Settlement> settlements = settlementRepository.findAll();
        assertThat(settlements).hasSize(1); // 중복 생성 안 됨
    }

    @Test
    @DisplayName("정산 스케줄러 실행 성공 - 정산 대상 없으면 생성 안 됨")
    void generateWeeklySettlement_noTarget() {

        paymentRepository.deleteAll();
        reservationRepository.deleteAll();

        settlementScheduler.generateWeeklySettlement();

        List<Settlement> settlements = settlementRepository.findAll();
        assertThat(settlements).isEmpty();
    }
}