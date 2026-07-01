package com.back.ovengers.domain.timedeal.service;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.timedeal.dto.TimeDealCreateRequest;
import com.back.ovengers.domain.timedeal.dto.TimeDealResponse;
import com.back.ovengers.domain.timedeal.dto.TimeDealUpdateRequest;
import com.back.ovengers.domain.timedeal.entity.TimeDeal;
import com.back.ovengers.domain.timedeal.entity.TimeDealStatus;
import com.back.ovengers.domain.timedeal.repository.TimeDealRepository;
import com.back.ovengers.domain.user.entity.Role;
import com.back.ovengers.domain.user.entity.Status;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.domain.user.repository.UserRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TimeDealServiceTest {

    @Autowired private TimeDealService timeDealService;
    @Autowired private UserRepository userRepository;
    @Autowired private CampingRepository campingRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private TimeDealRepository timeDealRepository;

    private User host;
    private User anotherHost;
    private Site site;

    @BeforeEach
    void setUp() {
        host = userRepository.save(User.builder()
                .email("host@test.com").password("1234").name("host")
                .nickname("host").phone("010").role(Role.HOST).status(Status.ACTIVE).build());

        anotherHost = userRepository.save(User.builder()
                .email("other@test.com").password("1234").name("other")
                .nickname("other").phone("010").role(Role.HOST).status(Status.ACTIVE).build());

        Camping camping = campingRepository.save(Camping.builder()
                .name("캠핑장").region("강원").city("강릉").address("강릉")
                .status(CampingStatus.APPROVED).host(host).build());

        site = siteRepository.save(Site.builder()
                .camping(camping).name("A사이트").baseCapacity(2).maxCapacity(4)
                .totalAmount(10).price(100000).build());
    }

    @AfterEach
    void tearDown() {
        // 동시성 테스트 등으로 인해 남은 데이터를 수동으로 비워 트랜잭션 격리 효과를 냅니다.
        timeDealRepository.deleteAllInBatch();
        siteRepository.deleteAllInBatch();
        campingRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @Transactional
    @DisplayName("타임딜 생성 성공")
    void create_success() {
        TimeDealCreateRequest request = new TimeDealCreateRequest(
                site.getId(), LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
                5, 70000, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1)
        );

        TimeDealResponse response = timeDealService.createTimeDeal(host.getId(), request);

        assertThat(response.siteId()).isEqualTo(site.getId());
        assertThat(response.quantity()).isEqualTo(5);
        assertThat(timeDealRepository.count()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("타임딜 생성 실패 - 할인 가격이 원가 이상")
    void create_fail_invalid_price() {
        TimeDealCreateRequest request = new TimeDealCreateRequest(
                site.getId(), LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
                5, 100000, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> timeDealService.createTimeDeal(host.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TIME_DEAL_INVALID_PRICE.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("타임딜 수정 성공")
    void update_success() {
        TimeDeal timeDeal = createTimeDeal();
        TimeDealUpdateRequest request = new TimeDealUpdateRequest(
                7, 60000, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusDays(2)
        );

        TimeDealResponse response = timeDealService.updateTimeDeal(host.getId(), timeDeal.getId(), request);

        assertThat(response.quantity()).isEqualTo(7);
        assertThat(response.dealPrice()).isEqualTo(60000);
    }

    @Test
    @Transactional
    @DisplayName("타임딜 수정 실패 - 권한 없음 (타인의 타임딜)")
    void update_fail_unauthorized() {
        TimeDeal timeDeal = createTimeDeal();
        TimeDealUpdateRequest request = new TimeDealUpdateRequest(
                7, 60000, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusDays(2)
        );

        assertThatThrownBy(() -> timeDealService.updateTimeDeal(anotherHost.getId(), timeDeal.getId(), request))
                .isInstanceOf(CustomException.class); // 기획한 ErrorCode가 있다면 .hasMessageContaining() 추가
    }

    @Test
    @Transactional
    @DisplayName("타임딜 취소 성공")
    void cancel_success() {
        TimeDeal timeDeal = createTimeDeal();

        timeDealService.cancelTimeDeal(host.getId(), timeDeal.getId());
        TimeDeal saved = timeDealRepository.findById(timeDeal.getId()).orElseThrow();

        assertThat(saved.getStatus()).isEqualTo(TimeDealStatus.CANCELLED);
    }

    @Test
    @Transactional
    @DisplayName("타임딜 삭제 성공")
    void delete_success() {
        TimeDeal timeDeal = createTimeDeal();

        timeDealService.deleteTimeDeal(host.getId(), timeDeal.getId());
        TimeDeal saved = timeDealRepository.findById(timeDeal.getId()).orElseThrow();

        assertThat(saved.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("타임딜 구매 동시성 테스트 - 한정된 수량에 다수 요청 선착순 검증")
    void purchase_concurrency() throws Exception {
        // given
        int totalQuantity = 5;
        TimeDeal timeDeal = timeDealRepository.save(
                TimeDeal.builder()
                        .site(site)
                        .checkIn(LocalDate.now().plusDays(10))
                        .checkOut(LocalDate.now().plusDays(12))
                        .quantity(totalQuantity)
                        .originalPrice(site.getPrice())
                        .dealPrice(70000)
                        .saleStartAt(LocalDateTime.now().minusHours(1)) // 즉시 구매 가능하도록 과거 시간 설정
                        .saleEndAt(LocalDateTime.now().plusDays(1))
                        .build()
        );

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // 비즈니스 로직 내부에서 비관적 락(Pessimistic Lock) 혹은 분산 락이 걸려있어야 정상 동작합니다.
                    timeDealService.purchaseTimeDeal(timeDeal.getId(), 1);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // then
        TimeDeal result = timeDealRepository.findById(timeDeal.getId()).orElseThrow();

        // 락 메커니즘이 잘 짜여있다면 정확히 재고 수량(5개)만큼만 성공해야 합니다.
        assertThat(successCount.get()).isEqualTo(totalQuantity);
        assertThat(result.getSoldCount()).isEqualTo(totalQuantity);
        assertThat(failCount.get()).isEqualTo(threadCount - totalQuantity);
    }

    @Test
    @Transactional
    @DisplayName("타임딜 단건 조회 성공")
    void getTimeDeal_success() {
        TimeDeal timeDeal = createActiveTimeDeal();

        TimeDealResponse response = timeDealService.getTimeDeal(timeDeal.getId());

        assertThat(response.siteId()).isEqualTo(site.getId());
    }

    @Test
    @Transactional
    @DisplayName("타임딜 단건 조회 실패 - 존재하지 않음")
    void getTimeDeal_fail_notFound() {
        assertThatThrownBy(() -> timeDealService.getTimeDeal(99999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TIME_DEAL_NOT_FOUND.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("내 타임딜 목록 조회")
    void getMyTimeDeals() {
        createTimeDeal();
        createTimeDeal();

        var result = timeDealService.getMyTimeDeals(host.getId(),
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @Transactional
    @DisplayName("활성 타임딜 목록 조회")
    void getActiveTimeDeals() {
        createActiveTimeDeal();

        var result = timeDealService.getActiveTimeDeals(
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @Transactional
    @DisplayName("타임딜 생성 실패 - 존재하지 않는 사이트")
    void create_fail_siteNotFound() {
        TimeDealCreateRequest request = new TimeDealCreateRequest(
                99999L, LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
                5, 70000, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> timeDealService.createTimeDeal(host.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.SITE_NOT_FOUND.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("타임딜 생성 실패 - 다른 호스트 사이트")
    void create_fail_accessDenied() {
        TimeDealCreateRequest request = new TimeDealCreateRequest(
                site.getId(), LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
                5, 70000, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> timeDealService.createTimeDeal(anotherHost.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("타임딜 생성 실패 - 과거 날짜")
    void create_fail_pastDate() {
        TimeDealCreateRequest request = new TimeDealCreateRequest(
                site.getId(), LocalDate.now().minusDays(1), LocalDate.now().plusDays(1),
                5, 70000, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> timeDealService.createTimeDeal(host.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TIME_DEAL_PAST_DATE.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("타임딜 생성 실패 - checkIn >= checkOut")
    void create_fail_invalidDateRange() {
        TimeDealCreateRequest request = new TimeDealCreateRequest(
                site.getId(), LocalDate.now().plusDays(10), LocalDate.now().plusDays(10),
                5, 70000, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1)
        );

        assertThatThrownBy(() -> timeDealService.createTimeDeal(host.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TIME_DEAL_INVALID_DATE_RANGE.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("타임딜 생성 실패 - 판매 종료 시각이 체크인 이후")
    void create_fail_invalidSaleWindow() {
        TimeDealCreateRequest request = new TimeDealCreateRequest(
                site.getId(), LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
                5, 70000, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(11)
        );

        assertThatThrownBy(() -> timeDealService.createTimeDeal(host.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TIME_DEAL_INVALID_SALE_WINDOW.getMessage());
    }

    @Test
    @Transactional
    @DisplayName("타임딜 삭제 실패 - 이미 판매된 경우")
    void delete_fail_alreadySold() {
        TimeDeal timeDeal = createActiveTimeDeal();
        timeDealRepository.purchaseAtomically(timeDeal.getId(), 1, TimeDealStatus.ACTIVE);

        assertThatThrownBy(() -> timeDealService.deleteTimeDeal(host.getId(), timeDeal.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TIME_DEAL_ALREADY_SOLD.getMessage());
    }

    @Test
    @DisplayName("타임딜 구매 실패 - 재고 초과")
    void purchase_fail_stockExceeded() {
        TimeDeal timeDeal = createActiveTimeDeal();

        assertThatThrownBy(() -> timeDealService.purchaseTimeDeal(timeDeal.getId(), 100))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.TIME_DEAL_STOCK_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("타임딜 구매 실패 - 존재하지 않음")
    void purchase_fail_notFound() {
        assertThatThrownBy(() -> timeDealService.purchaseTimeDeal(99999L, 1))
                .isInstanceOf(CustomException.class);
    }

    private TimeDeal createTimeDeal() {
        return timeDealRepository.save(
                TimeDeal.builder()
                        .site(site)
                        .checkIn(LocalDate.now().plusDays(10))
                        .checkOut(LocalDate.now().plusDays(12))
                        .quantity(5)
                        .originalPrice(site.getPrice())
                        .dealPrice(70000)
                        .saleStartAt(LocalDateTime.now().plusHours(1))
                        .saleEndAt(LocalDateTime.now().plusDays(1))
                        .build()
        );
    }

    private TimeDeal createActiveTimeDeal() {
        return timeDealRepository.save(
                TimeDeal.builder()
                        .site(site)
                        .checkIn(LocalDate.now().plusDays(10))
                        .checkOut(LocalDate.now().plusDays(12))
                        .quantity(5)
                        .originalPrice(site.getPrice())
                        .dealPrice(70000)
                        .saleStartAt(LocalDateTime.now().minusHours(1))
                        .saleEndAt(LocalDateTime.now().plusDays(1))
                        .build()
        );
    }
}