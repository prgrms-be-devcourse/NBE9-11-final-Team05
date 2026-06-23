package com.back.ovengers.domain.timedeal.service;

import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.site.entity.Site;
import com.back.ovengers.domain.site.repository.SiteRepository;
import com.back.ovengers.domain.timedeal.dto.TimeDealCreateRequest;
import com.back.ovengers.domain.timedeal.dto.TimeDealResponse;
import com.back.ovengers.domain.timedeal.dto.TimeDealUpdateRequest;
import com.back.ovengers.domain.timedeal.entity.TimeDeal;
import com.back.ovengers.domain.timedeal.entity.TimeDealStatus;
import com.back.ovengers.domain.timedeal.repository.TimeDealRepository;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeDealService {

    private final TimeDealRepository timeDealRepository;
    private final SiteRepository siteRepository;
    private final ReservationRepository reservationRepository;

    // ──────────────────────────────────────────────
    // 호스트 관리 API
    // ──────────────────────────────────────────────

    /**
     * 타임딜 생성
     *
     * [동시성 포인트] validateAvailableQuantity 내부에서 Site를 비관적 락으로 조회.
     * 동일 Site에 대한 타임딜 생성 요청이 동시에 들어올 경우 직렬화된다.
     */
    @Transactional
    public TimeDealResponse createTimeDeal(Long hostId, TimeDealCreateRequest request) {

        // 비관적 락으로 Site 조회: 수량 검증과 저장을 원자적 구간으로 보호
        Site site = siteRepository.findByIdWithLock(request.siteId())
                .orElseThrow(() -> new CustomException(ErrorCode.SITE_NOT_FOUND));

        validateHostOwnership(site, hostId);
        validateDateRange(request.checkIn(), request.checkOut());
        validateSaleWindow(request.saleStartAt(), request.saleEndAt(), request.checkIn());
        validateDiscountPrice(site.getPrice(), request.dealPrice());
        validateAvailableQuantity(site, request.checkIn(), request.checkOut(), request.quantity(), null);

        TimeDeal timeDeal = TimeDeal.builder()
                .site(site)
                .checkIn(request.checkIn())
                .checkOut(request.checkOut())
                .quantity(request.quantity())
                .originalPrice(site.getPrice())
                .dealPrice(request.dealPrice())
                .saleStartAt(request.saleStartAt())
                .saleEndAt(request.saleEndAt())
                .build();

        return TimeDealResponse.from(timeDealRepository.save(timeDeal));
    }

    /**
     * 타임딜 수정
     *
     * [동시성 포인트]
     * 1. validateAvailableQuantity 내부에서 Site 비관적 락 → 수량 검증 직렬화
     * 2. TimeDeal의 @Version → 동시 수정 시 ObjectOptimisticLockingFailureException 발생
     *    (GlobalExceptionHandler에서 409 Conflict 등으로 처리 권장)
     */
    @Transactional
    public TimeDealResponse updateTimeDeal(Long hostId, Long timeDealId, TimeDealUpdateRequest request) {
        try {
            TimeDeal timeDeal = getOwnedTimeDeal(hostId, timeDealId);

            validateSaleWindow(request.saleStartAt(), request.saleEndAt(), timeDeal.getCheckIn());
            validateDiscountPrice(timeDeal.getOriginalPrice(), request.dealPrice());
            validateAvailableQuantity(
                    timeDeal.getSite(),
                    timeDeal.getCheckIn(),
                    timeDeal.getCheckOut(),
                    request.quantity(),
                    timeDeal.getId()
            );

            timeDeal.update(request.quantity(), request.dealPrice(), request.saleStartAt(), request.saleEndAt());
            return TimeDealResponse.from(timeDeal);

        } catch (ObjectOptimisticLockingFailureException e) {
            // 동시 수정 충돌: 다른 트랜잭션이 먼저 수정을 완료한 상황
            throw new CustomException(ErrorCode.TIME_DEAL_CONCURRENT_MODIFICATION);
        }
    }

    /**
     * 타임딜 취소
     *
     * [동시성 포인트] 비관적 락으로 TimeDeal 조회
     *
     * 왜 취소에 비관적 락을 사용하는가?
     *   - 구매(purchaseAtomically)와 취소가 동시에 발생할 수 있음
     *   - 취소 트랜잭션이 TimeDeal을 SELECT FOR UPDATE로 잠그면,
     *     진행 중인 구매의 UPDATE문이 취소 커밋 후에 실행된다.
     *   - 이후 구매 UPDATE는 status = 'ACTIVE' 조건이 맞지 않아 0 rows → 구매 실패
     *   - @Version만 사용하면 구매는 TimeDeal을 JPA로 읽지 않아 version 충돌이 감지되지 않음
     */
    @Transactional
    public void cancelTimeDeal(Long hostId, Long timeDealId) {
        TimeDeal timeDeal = getOwnedTimeDealWithLock(hostId, timeDealId);
        timeDeal.cancel();
    }

    /**
     * 타임딜 삭제 (소프트 딜리트)
     *
     * [동시성 포인트] 비관적 락으로 TimeDeal 조회
     * soldCount > 0 체크 + softDelete를 하나의 잠긴 구간으로 보호
     */
    @Transactional
    public void deleteTimeDeal(Long hostId, Long timeDealId) {
        TimeDeal timeDeal = getOwnedTimeDealWithLock(hostId, timeDealId);

        if (timeDeal.getSoldCount() > 0) {
            throw new CustomException(ErrorCode.TIME_DEAL_ALREADY_SOLD);
        }

        timeDeal.softDelete();
    }

    // ──────────────────────────────────────────────
    // 구매 API (예약 서비스와 연동)
    // ──────────────────────────────────────────────

    /**
     * 타임딜 구매 - soldCount 원자적 증가
     *
     * [동시성 해결 전략: 원자적 SQL UPDATE]
     *
     * 기존 방식의 문제 (Read-Modify-Write):
     *   1. SELECT 로 현재 soldCount 읽기
     *   2. 애플리케이션에서 soldCount + 1 계산
     *   3. UPDATE로 새 값 저장
     *   → 1번과 3번 사이에 다른 트랜잭션이 끼어들면 Lost Update 발생
     *   → 재고 초과 판매 (overselling)
     *
     * 원자적 UPDATE 방식:
     *   UPDATE time_deal
     *   SET sold_count = sold_count + :count
     *   WHERE id = :id
     *     AND status = 'ACTIVE'
     *     AND sold_count + :count <= quantity
     *
     *   → DB 엔진이 이 UPDATE를 원자적으로 실행
     *   → 동시 요청들은 DB 내부에서 자동으로 직렬화됨
     *   → 재고 초과 조건(sold_count + count <= quantity)이 WHERE에 포함되어
     *      재고가 부족하면 0 rows 반환 → 애플리케이션이 실패로 처리
     *
     * 낙관적 락(@Version)을 사용하지 않는 이유:
     *   - 타임딜은 플래시 세일 = 단시간 폭발적 트래픽
     *   - 충돌이 잦으면 재시도가 폭주 → DB 부하 급증 → 사실상 더 위험
     *   - 원자적 UPDATE는 재시도 없이 첫 번째 시도에 결과 확정
     *
     * 비관적 락(SELECT FOR UPDATE)을 사용하지 않는 이유:
     *   - SELECT 시점부터 커밋까지 행 잠금 유지
     *   - 모든 구매 요청이 직렬 대기 → 처리량(TPS) 급감
     *   - 원자적 UPDATE는 DB 내부에서만 짧게 직렬화 → 더 높은 처리량
     *
     * @param timeDealId 구매할 타임딜 ID
     * @param count 구매 수량
     */
    @Transactional
    public void purchaseTimeDeal(Long timeDealId, int count) {
        int affectedRows = timeDealRepository.purchaseAtomically(
                timeDealId, count, TimeDealStatus.ACTIVE
        );

        if (affectedRows == 0) {
            // 실패 원인 판별: 재고 부족 vs 판매 중 아님
            TimeDeal timeDeal = timeDealRepository.findActiveById(timeDealId)
                    .orElseThrow(() -> new CustomException(ErrorCode.TIME_DEAL_NOT_FOUND));

            if (timeDeal.getStatus() == TimeDealStatus.SOLD_OUT) {
                throw new CustomException(ErrorCode.TIME_DEAL_SOLD_OUT);
            }
            if (timeDeal.getStatus() != TimeDealStatus.ACTIVE) {
                throw new CustomException(ErrorCode.TIME_DEAL_NOT_ACTIVE);
            }
            // ACTIVE이지만 재고 부족 (sold_count + count > quantity)
            throw new CustomException(ErrorCode.TIME_DEAL_STOCK_EXCEEDED);
        }

        // 마지막 재고가 팔렸다면 SOLD_OUT으로 전환
        // 이 UPDATE는 sold_count >= quantity 인 경우에만 실행되므로 멱등성 보장
        timeDealRepository.markSoldOutIfExhausted(
                timeDealId, TimeDealStatus.SOLD_OUT, TimeDealStatus.ACTIVE
        );
    }

    // ──────────────────────────────────────────────
    // 조회 API
    // ──────────────────────────────────────────────

    public Page<TimeDealResponse> getMyTimeDeals(Long hostId, Pageable pageable) {
        return timeDealRepository
                .findAllByHostId(hostId, pageable)
                .map(TimeDealResponse::from);
    }

    public Page<TimeDealResponse> getActiveTimeDeals(Pageable pageable) {
        return timeDealRepository
                .findAllActive(LocalDateTime.now(), pageable)
                .map(TimeDealResponse::from);
    }

    public TimeDealResponse getTimeDeal(Long timeDealId) {
        return TimeDealResponse.from(
                timeDealRepository.findActiveById(timeDealId)
                        .orElseThrow(() -> new CustomException(ErrorCode.TIME_DEAL_NOT_FOUND))
        );
    }

    // ──────────────────────────────────────────────
    // private 헬퍼
    // ──────────────────────────────────────────────

    /**
     * 일반 조회 (낙관적 락 @Version 동작)
     * 수정 내용 저장 시 JPA가 version을 체크하여 충돌 감지
     */
    private TimeDeal getOwnedTimeDeal(Long hostId, Long timeDealId) {
        TimeDeal timeDeal = timeDealRepository.findActiveById(timeDealId)
                .orElseThrow(() -> new CustomException(ErrorCode.TIME_DEAL_NOT_FOUND));
        if (!timeDeal.isOwnedBy(hostId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return timeDeal;
    }

    /**
     * 비관적 락으로 조회 (SELECT FOR UPDATE)
     * 취소/삭제처럼 상태 전환의 원자성이 중요한 경우 사용
     */
    private TimeDeal getOwnedTimeDealWithLock(Long hostId, Long timeDealId) {
        TimeDeal timeDeal = timeDealRepository.findActiveByIdWithLock(timeDealId)
                .orElseThrow(() -> new CustomException(ErrorCode.TIME_DEAL_NOT_FOUND));
        if (!timeDeal.isOwnedBy(hostId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return timeDeal;
    }

    private void validateHostOwnership(Site site, Long hostId) {
        if (!site.getCamping().getHost().getId().equals(hostId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut)) {
            throw new CustomException(ErrorCode.TIME_DEAL_INVALID_DATE_RANGE);
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.TIME_DEAL_PAST_DATE);
        }
    }

    private void validateSaleWindow(LocalDateTime start, LocalDateTime end, LocalDate checkIn) {
        if (!start.isBefore(end)) {
            throw new CustomException(ErrorCode.TIME_DEAL_INVALID_SALE_WINDOW);
        }
        if (end.isAfter(checkIn.atStartOfDay())) {
            throw new CustomException(ErrorCode.TIME_DEAL_INVALID_SALE_WINDOW);
        }
    }

    private void validateDiscountPrice(int originalPrice, int dealPrice) {
        if (dealPrice >= originalPrice) {
            throw new CustomException(ErrorCode.TIME_DEAL_INVALID_PRICE);
        }
    }

    /**
     * 타임딜 등록/수정 가능한 잔여 수량 검증
     *
     * [비관적 락 적용 지점]
     * createTimeDeal / updateTimeDeal 진입 시 Site를 findByIdWithLock()으로 조회하므로
     * 이 메서드가 실행될 때 이미 Site 행에 배타 락이 걸려 있다.
     * 동일 Site에 대한 다른 타임딜 생성/수정 요청은 락 해제까지 대기한다.
     *
     * 잔여수량 = Site 총 재고 - 이미 확정된 예약 수 - 다른 타임딜에 할당된 수량
     */
    private void validateAvailableQuantity(Site site, LocalDate checkIn, LocalDate checkOut,
                                           int requestedQuantity, Long excludeTimeDealId) {
        long reservedCount = reservationRepository
                .countActiveReservationsByOverlappingDates(site.getId(), checkIn, checkOut);

        int allocatedByOtherDeals = timeDealRepository
                .sumReservedQuantityByOverlappingDates(site.getId(), checkIn, checkOut, excludeTimeDealId);

        int available = site.getTotalAmount() - (int) reservedCount - allocatedByOtherDeals;

        if (requestedQuantity > available) {
            throw new CustomException(ErrorCode.TIME_DEAL_STOCK_EXCEEDED);
        }
    }
}