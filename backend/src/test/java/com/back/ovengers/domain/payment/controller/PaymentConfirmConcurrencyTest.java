package com.back.ovengers.domain.payment.controller;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.payment.client.TossPaymentClient;
import com.back.ovengers.domain.payment.dto.PaymentConfirmRequest;
import com.back.ovengers.domain.payment.dto.TossConfirmResponse;
import com.back.ovengers.domain.payment.entity.Payment;
import com.back.ovengers.domain.payment.entity.PaymentStatus;
import com.back.ovengers.domain.payment.repository.PaymentRepository;
import com.back.ovengers.domain.payment.scheduler.PaymentScheduler;
import com.back.ovengers.domain.payment.service.PaymentService;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.reservation.service.ReservationService;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.fixture.UserFixture;
import com.back.ovengers.global.security.JwtProvider;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Disabled("동시성 테스트 - 별도 MySQL 환경 필요, 수동 실행")
class PaymentConfirmConcurrencyTest {

    @Autowired private PaymentService paymentService;
    @Autowired private ReservationService reservationService;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private CampingRepository campingRepository;
    @Autowired private DataSource dataSource;
    @Autowired private PaymentScheduler paymentScheduler;

    @MockitoBean
    private TossPaymentClient tossPaymentClient;

    private User user;
    private Reservation reservation;
    private Payment payment;


    private void cleanDatabase() {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("TRUNCATE TABLE notification");
            stmt.execute("TRUNCATE TABLE settlement_detail");
            stmt.execute("TRUNCATE TABLE settlement");
            stmt.execute("TRUNCATE TABLE review");
            stmt.execute("TRUNCATE TABLE payment");
            stmt.execute("TRUNCATE TABLE reservation");
            stmt.execute("TRUNCATE TABLE site");
            stmt.execute("TRUNCATE TABLE camping_image");
            stmt.execute("TRUNCATE TABLE camping");
            stmt.execute("TRUNCATE TABLE refresh_tokens");
            stmt.execute("TRUNCATE TABLE users");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        cleanDatabase();
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(
                UserFixture.user()
                        .email("user" + System.nanoTime() + "@test.com")
                        .nickname("유저" + System.nanoTime())
                        .build()
        );

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
                .checkIn(LocalDate.now().plusDays(1))
                .checkOut(LocalDate.now().plusDays(3))
                .guestCount(2)
                .rsvPrice(100000)
                .status(ReservationStatus.PENDING)
                .build());

        payment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ORD-" + UUID.randomUUID())
                .paidPrice(100000)
                .status(PaymentStatus.READY)
                .build());
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        siteRepository.deleteAll();
        campingRepository.deleteAll();
        userRepository.deleteAll();
    }

    private PaymentConfirmRequest makeConfirmRequest() {
        return new PaymentConfirmRequest(
                "test_payment_key",
                payment.getOrderId(),
                100000
        );
    }

    private TossConfirmResponse makeTossResponse(String orderId) {
        return new TossConfirmResponse(
                "test_payment_key",
                orderId,
                "카드",
                100000,
                "2026-06-25T10:00:00+09:00",
                "DONE"
        );
    }


    @Test
    @DisplayName("한 예약에 여러 결제 생성 후 동시에 각각 confirm 시도 - 1개만 성공해야 함")
    void multiplePayments_concurrentConfirm_onlyOneSuccess() throws InterruptedException {

        // 기존 payment(READY) 1개 + 추가 4개 = 총 5개 결제 생성 (모두 같은 예약)
        List<Payment> payments = new java.util.ArrayList<>();
        payments.add(payment);

        for (int i = 0; i < 4; i++) {
            payments.add(paymentRepository.save(Payment.builder()
                    .reservation(reservation)
                    .orderId("ORD-" + UUID.randomUUID())
                    .paidPrice(100000)
                    .status(PaymentStatus.READY)
                    .build()));
        }

        // 토스 confirm은 항상 성공한다고 가정
        given(tossPaymentClient.confirm(any(), any(), any()))
                .willAnswer(invocation -> makeTossResponse(invocation.getArgument(1)));

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 5개 스레드가 각자 다른 결제건으로 동시에 confirm 시도
        for (int i = 0; i < threadCount; i++) {
            final Payment p = payments.get(i);
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    PaymentConfirmRequest request = new PaymentConfirmRequest(
                            "test_payment_key_" + p.getOrderId(),
                            p.getOrderId(),
                            100000
                    );

                    paymentService.confirm(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("실패: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        System.out.println("결제 승인 성공: " + successCount.get());
        System.out.println("결제 승인 실패: " + failCount.get());

        // ── 검증 ──────────────────────────────────────────────

        // 1) 정확히 1개만 성공
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(4);

        List<Payment> allPayments = paymentRepository.findAllByReservation_Id(reservation.getId());

        // 2) DONE 결제는 정확히 1개 (중복 결제 없음 - 핵심 불변식)
        long doneCount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.DONE)
                .count();
        assertThat(doneCount).isEqualTo(1);

        // 3) 예약은 CONFIRMED
        Reservation updatedReservation = reservationRepository
                .findById(reservation.getId()).orElseThrow();
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        // 4) IN_PROGRESS로 남은 결제가 있으면 안 됨 (선점 후 정상 복구/미진입)
        long inProgressCount = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.IN_PROGRESS)
                .count();
        assertThat(inProgressCount).isEqualTo(0);

        // 5) DONE 1개를 제외한 나머지 4개는 READY 또는 CANCELLED
        //    - postConfirm이 즉시 잡은 건 CANCELLED
        //    - 못 잡은 건 READY로 남아 30분 뒤 스케줄러가 정리
        //    동시성 타이밍에 따라 비율이 갈리므로 둘을 합쳐서 4개인지 검증
        long readyOrCancelled = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.READY
                        || p.getStatus() == PaymentStatus.CANCELLED)
                .count();
        assertThat(readyOrCancelled).isEqualTo(4);
    }


    @Test
    @DisplayName("결제 승인 + 예약 취소 동시 실행 - 둘 중 하나만 성공해야 함")
    void confirmPayment_and_cancelReservation_concurrently() throws InterruptedException {

        // 메서드 레벨에서 선언 (람다에서 접근 가능)
        final Payment newPayment = paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .orderId("ORD-NEW-" + UUID.randomUUID())
                .paidPrice(100000)
                .status(PaymentStatus.READY)
                .build());

        reservation.updateStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        payment.confirm("test_payment_key");
        paymentRepository.save(payment);

        given(tossPaymentClient.confirm(any(), any(), any()))
                .willAnswer(invocation -> makeTossResponse(invocation.getArgument(1)));
        doNothing().when(tossPaymentClient).cancel(any(), any());

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger confirmSuccess = new AtomicInteger(0);
        AtomicInteger cancelSuccess = new AtomicInteger(0);
        AtomicInteger confirmFail = new AtomicInteger(0);
        AtomicInteger cancelFail = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // 스레드 1: 결제 승인 시도
        executorService.submit(() -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest(
                        "test_payment_key",
                        newPayment.getOrderId(),
                        100000
                );

                paymentService.confirm(confirmRequest);
                confirmSuccess.incrementAndGet();
            } catch (Exception e) {
                System.out.println("결제 승인 실패: " + e.getMessage());
                confirmFail.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        // 스레드 2: 예약 취소 시도
        executorService.submit(() -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                reservationService.cancelReservation(reservation.getId(), user.getId());
                cancelSuccess.incrementAndGet();
            } catch (Exception e) {
                System.out.println("예약 취소 실패: " + e.getMessage());
                cancelFail.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        System.out.println("결제 승인 성공: " + confirmSuccess.get());
        System.out.println("결제 승인 실패: " + confirmFail.get());
        System.out.println("예약 취소 성공: " + cancelSuccess.get());
        System.out.println("예약 취소 실패: " + cancelFail.get());

        assertThat(confirmSuccess.get() + cancelSuccess.get()).isEqualTo(1);

        Reservation finalReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
        List<Payment> finalPayments = paymentRepository.findAllByReservation_Id(reservation.getId());

        if (finalReservation.getStatus() == ReservationStatus.CONFIRMED) {
            long doneCount = finalPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.DONE)
                    .count();
            assertThat(doneCount).isEqualTo(1);
        } else if (finalReservation.getStatus() == ReservationStatus.CANCELLED) {
            long doneCount = finalPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.DONE)
                    .count();
            assertThat(doneCount).isEqualTo(0);
        } else {
            throw new AssertionError("예약 상태가 중간값: " + finalReservation.getStatus());
        }
    }

    @Test
    @DisplayName("같은 예약에 동시에 취소 요청 2개 - 1개만 성공하고 상태 불일치 없어야 함")
    void cancelReservation_concurrently_onlyOneSuccess() throws InterruptedException {

        // CONFIRMED 상태로 세팅
        reservation.updateStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        payment.confirm("test_payment_key");
        paymentRepository.save(payment);

        doNothing().when(tossPaymentClient).cancel(any(), any());

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    reservationService.cancelReservation(reservation.getId(), user.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("취소 실패: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        System.out.println("취소 성공: " + successCount.get());
        System.out.println("취소 실패: " + failCount.get());

        // 1개만 성공해야 함
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(1);

        // Reservation CANCELLED 상태 확인
        Reservation finalReservation = reservationRepository
                .findById(reservation.getId()).orElseThrow();
        assertThat(finalReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);

        // Payment CANCELLED 상태 확인
        Payment finalPayment = paymentRepository
                .findById(payment.getId()).orElseThrow();
        assertThat(finalPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);

        // 상태 불일치 없어야 함
        // CONFIRMED + CANCELLED 조합 또는 CANCELLED + DONE 조합이 나오면 안 됨
        boolean isConsistent =
                (finalReservation.getStatus() == ReservationStatus.CANCELLED &&
                        finalPayment.getStatus() == PaymentStatus.CANCELLED);

        assertThat(isConsistent)
                .withFailMessage("상태 불일치 발생! Reservation: %s, Payment: %s",
                        finalReservation.getStatus(), finalPayment.getStatus())
                .isTrue();
    }
    @Test
    @DisplayName("스케줄러: CONFIRMED 예약의 잉여 READY 결제는 취소되고 예약은 보존된다")
    void scheduler_cleansUpOrphanReady_keepsConfirmedReservation() {

        // given: 예약 CONFIRMED + DONE 1건 + 30분 지난 READY 잉여 결제 4건
        reservation.updateStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        payment.confirm("test_payment_key");
        paymentRepository.save(payment);  // DONE

        LocalDateTime past = LocalDateTime.now().minusMinutes(40);
        for (int i = 0; i < 4; i++) {
            Payment orphan = Payment.builder()
                    .reservation(reservation)
                    .orderId("ORD-ORPHAN-" + UUID.randomUUID())
                    .paidPrice(100000)
                    .status(PaymentStatus.READY)
                    .build();
            Payment saved = paymentRepository.save(orphan);
            // createdAt을 과거로 강제 (스케줄러 조건 충족시키기)
            forceCreatedAt(saved.getId(), past);
        }

        // when: 스케줄러 실행
        paymentScheduler.expirePendingPayments();

        // then: 잉여 READY 4건은 CANCELLED, 예약은 CONFIRMED 보존
        List<Payment> all = paymentRepository.findAllByReservation_Id(reservation.getId());
        long cancelled = all.stream().filter(p -> p.getStatus() == PaymentStatus.CANCELLED).count();
        long done = all.stream().filter(p -> p.getStatus() == PaymentStatus.DONE).count();
        assertThat(cancelled).isEqualTo(4);
        assertThat(done).isEqualTo(1);

        Reservation finalRsv = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(finalRsv.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);  // 보존!
    }

    // createdAt을 강제로 과거로 세팅 (스케줄러의 30분 조건 충족용)
    private void forceCreatedAt(Long paymentId, LocalDateTime time) {
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(
                     "UPDATE payment SET created_at = ? WHERE id = ?")) {
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(time));
            stmt.setLong(2, paymentId);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
