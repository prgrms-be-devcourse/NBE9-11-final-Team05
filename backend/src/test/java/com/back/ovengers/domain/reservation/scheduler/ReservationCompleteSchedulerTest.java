package com.back.ovengers.domain.reservation.scheduler;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ReservationCompleteSchedulerTest {

    @Autowired private ReservationCompleteScheduler scheduler;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private CampingRepository campingRepository;

    private Site site;

    @BeforeEach
    void setUp() {
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

        Camping camping = campingRepository.save(Camping.builder()
                .host(host)
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
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Reservation createReservation(LocalDate checkOut, ReservationStatus status) {

        User user = userRepository.save(
                UserFixture.user()
                        .email("user" + System.nanoTime() + "@test.com")
                        .nickname("유저" + System.nanoTime())
                        .build()
        );

        return reservationRepository.save(Reservation.builder()
                .user(user)
                .site(site)
                .rsvNum("RSV-" + UUID.randomUUID())
                .rsvName("홍길동")
                .rsvPhone("010-1234-5678")
                .checkIn(checkOut.minusDays(2))
                .checkOut(checkOut)
                .guestCount(2)
                .rsvPrice(100000)
                .status(status)
                .build());
    }

    @Test
    @DisplayName("체크아웃이 오늘인 CONFIRMED 예약은 COMPLETED 처리된다")
    void completeExpiredReservations_checkOutToday() {

        Reservation reservation = createReservation(LocalDate.now(), ReservationStatus.CONFIRMED);

        scheduler.completeExpiredReservations();

        Reservation result = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test
    @DisplayName("체크아웃이 과거인 CONFIRMED 예약은 COMPLETED 처리된다")
    void completeExpiredReservations_checkOutPast() {

        Reservation reservation = createReservation(LocalDate.now().minusDays(3), ReservationStatus.CONFIRMED);

        scheduler.completeExpiredReservations();

        Reservation result = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test
    @DisplayName("체크아웃이 미래인 CONFIRMED 예약은 처리되지 않는다")
    void completeExpiredReservations_checkOutFuture() {

        Reservation reservation = createReservation(LocalDate.now().plusDays(3), ReservationStatus.CONFIRMED);

        scheduler.completeExpiredReservations();

        Reservation result = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("PENDING 상태 예약은 체크아웃 지나도 처리되지 않는다")
    void completeExpiredReservations_pendingNotAffected() {

        Reservation reservation = createReservation(LocalDate.now().minusDays(1), ReservationStatus.PENDING);

        scheduler.completeExpiredReservations();

        Reservation result = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("이미 COMPLETED인 예약은 영향받지 않는다")
    void completeExpiredReservations_alreadyCompleted() {

        Reservation reservation = createReservation(LocalDate.now().minusDays(5), ReservationStatus.COMPLETED);

        scheduler.completeExpiredReservations();

        Reservation result = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test
    @DisplayName("여러 예약 중 조건에 맞는 것만 COMPLETED 처리된다")
    void completeExpiredReservations_multipleReservations() {

        Reservation pastReservation = createReservation(LocalDate.now().minusDays(2), ReservationStatus.CONFIRMED);
        Reservation todayReservation = createReservation(LocalDate.now(), ReservationStatus.CONFIRMED);
        Reservation futureReservation = createReservation(LocalDate.now().plusDays(2), ReservationStatus.CONFIRMED);

        scheduler.completeExpiredReservations();

        assertThat(reservationRepository.findById(pastReservation.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.COMPLETED);
        assertThat(reservationRepository.findById(todayReservation.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.COMPLETED);
        assertThat(reservationRepository.findById(futureReservation.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
    }
}
