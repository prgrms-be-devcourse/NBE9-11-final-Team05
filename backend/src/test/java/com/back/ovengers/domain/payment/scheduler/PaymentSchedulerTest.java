package com.back.ovengers.domain.payment.scheduler;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PaymentSchedulerTest {

    @Autowired
    private PaymentScheduler paymentScheduler;

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

    private Reservation createReservation() {

        User user = userRepository.save(User.builder()
                .email("test" + System.nanoTime() + "@test.com")
                .password("1234")
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

        Site site = siteRepository.save(Site.builder()
                .camping(camping)
                .name("A구역")
                .baseCapacity(2)
                .maxCapacity(4)
                .totalAmount(5)
                .price(50000)
                .build());

        return reservationRepository.save(Reservation.builder()
                .user(user)
                .site(site)
                .rsvNum("ABC-20260615-A1B2C3D4")
                .rsvName("홍길동")
                .rsvPhone("010-1234-5678")
                .checkIn(LocalDate.now().plusDays(1))
                .checkOut(LocalDate.now().plusDays(2))
                .guestCount(2)
                .rsvPrice(50000)
                .status(ReservationStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("30분 지난 READY 결제는 자동 취소되고 예약도 취소된다")
    void expirePendingPayments_success() {

        // given
        Reservation reservation = createReservation();

        Payment payment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ORD-20260615-AAAAAAAA")
                .paidPrice(50000)
                .status(PaymentStatus.READY)
                .build());

        // createdAt을 강제로 31분 전으로 변경
        ReflectionTestUtils.setField(payment, "createdAt",
                LocalDateTime.now().minusMinutes(31));
        paymentRepository.save(payment);

        // when
        paymentScheduler.expirePendingPayments();

        // then
        Payment resultPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        Reservation resultReservation = reservationRepository.findById(reservation.getId()).orElseThrow();

        assertThat(resultPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(resultReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    @DisplayName("30분 안 지난 READY 결제는 취소되지 않는다")
    void expirePendingPayments_notExpired() {

        // given
        Reservation reservation = createReservation();

        Payment payment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ORD-20260615-BBBBBBBB")
                .paidPrice(50000)
                .status(PaymentStatus.READY)
                .build());

        // when
        paymentScheduler.expirePendingPayments();

        // then
        Payment resultPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        Reservation resultReservation = reservationRepository.findById(reservation.getId()).orElseThrow();

        assertThat(resultPayment.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(resultReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("DONE 상태 결제는 만료 처리 대상에서 제외된다")
    void expirePendingPayments_doneStatusExcluded() {

        // given
        Reservation reservation = createReservation();

        Payment payment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ORD-20260615-CCCCCCCC")
                .paidPrice(50000)
                .status(PaymentStatus.DONE)
                .build());

        ReflectionTestUtils.setField(payment, "createdAt",
                LocalDateTime.now().minusMinutes(31));
        paymentRepository.save(payment);

        // when
        paymentScheduler.expirePendingPayments();

        // then
        Payment resultPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(resultPayment.getStatus()).isEqualTo(PaymentStatus.DONE);
    }
}
