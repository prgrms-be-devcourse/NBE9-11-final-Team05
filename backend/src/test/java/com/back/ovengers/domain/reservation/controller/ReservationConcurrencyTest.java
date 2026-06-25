package com.back.ovengers.domain.reservation.controller;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.dto.ReservationRequest;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.fixture.UserFixture;
import com.back.ovengers.global.security.JwtProvider;
import org.junit.jupiter.api.*;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("동시성 테스트 - 별도 MySQL 환경 필요, 수동 실행")
class ReservationConcurrencyTest {

    private RestTemplate restTemplate = new RestTemplate();
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private CampingRepository campingRepository;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private DataSource dataSource;

    @LocalServerPort
    private int port;

    private Site site;
    private List<String> accessTokens;

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
                .totalAmount(1)  // 재고 1개
                .price(50000)
                .build());

        accessTokens = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User user = userRepository.save(
                    UserFixture.user()
                            .email("user" + i + System.nanoTime() + "@test.com")
                            .nickname("유저" + i + System.nanoTime())
                            .build()
            );
            accessTokens.add(jwtProvider.createRefreshToken(user.getId(), user.getRole().name()));
        }
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    @DisplayName("재고 1개 남았을 때 동시 예약 시도 - 1명만 성공해야 함")
    void concurrentReservation_onlyOneSuccess() throws Exception {

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

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
        String baseUrl = "http://localhost:" + port + "/api/reservations";

        for (int i = 0; i < threadCount; i++) {
            final String token = accessTokens.get(i);
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add("Cookie", "accessToken=" + token);

                    HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

                    ResponseEntity<String> response = restTemplate.postForEntity(
                            baseUrl,
                            entity,
                            String.class
                    );

                    if (response.getStatusCode().value() == 201) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
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

        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);

        long reservationCount = reservationRepository.findAll()
                .stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .count();
        assertThat(reservationCount).isEqualTo(1);
    }
}
