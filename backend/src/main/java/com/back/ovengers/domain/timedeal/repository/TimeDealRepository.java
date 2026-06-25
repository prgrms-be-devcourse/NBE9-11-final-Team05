package com.back.ovengers.domain.timedeal.repository;

import com.back.ovengers.domain.timedeal.entity.TimeDeal;
import com.back.ovengers.domain.timedeal.entity.TimeDealStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TimeDealRepository extends JpaRepository<TimeDeal, Long> {

    /**
     * 삭제되지 않은 타임딜 단건 조회 (읽기 전용)
     */
    @Query("""
        SELECT td FROM TimeDeal td
        JOIN FETCH td.site s
        JOIN FETCH s.camping c
        WHERE td.id = :id AND td.deletedAt IS NULL
        """)
    Optional<TimeDeal> findActiveById(@Param("id") Long id);

    /**
     * [비관적 락] 타임딜 단건 조회 - 취소/삭제 동시 요청 직렬화용
     *
     * SELECT ... FOR UPDATE 를 발행하여 행 레벨 잠금을 획득한다.
     * 같은 행을 동시에 수정하려는 다른 트랜잭션은 이 트랜잭션이 커밋/롤백될 때까지 대기한다.
     *
     * 사용처: cancelTimeDeal, deleteTimeDeal
     * - 호스트가 동시에 취소 버튼을 두 번 클릭하거나, 구매 직전 취소 요청이 겹치는 경우를 방어
     *
     * timeout = 3000ms: 락 대기 중 3초 초과 시 LockTimeoutException 발생 (무한 대기 방지)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("""
        SELECT td FROM TimeDeal td
        JOIN FETCH td.site s
        JOIN FETCH s.camping c
        WHERE td.id = :id AND td.deletedAt IS NULL
        """)
    Optional<TimeDeal> findActiveByIdWithLock(@Param("id") Long id);

    /**
     * 특정 호스트가 등록한 타임딜 목록 조회
     */
    @Query(value = """
        SELECT td FROM TimeDeal td
        WHERE td.site.camping.host.id = :hostId
          AND td.deletedAt IS NULL
        ORDER BY td.createdAt DESC
        """,
            countQuery = """
        SELECT COUNT(td) FROM TimeDeal td
        WHERE td.site.camping.host.id = :hostId
          AND td.deletedAt IS NULL
        """)
    Page<TimeDeal> findAllByHostId(@Param("hostId") Long hostId, Pageable pageable);

    /**
     * 현재 판매 중인 타임딜 목록 조회
     */
    @Query("""
        SELECT td FROM TimeDeal td
        WHERE td.status = 'ACTIVE'
          AND td.deletedAt IS NULL
          AND td.saleStartAt <= :now AND td.saleEndAt > :now
        ORDER BY td.saleEndAt ASC
        """)
    Page<TimeDeal> findAllActive(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 동일 Site의 겹치는 기간에 할당된 타임딜 수량 합계 조회
     */
    @Query("""
        SELECT COALESCE(SUM(td.quantity), 0) FROM TimeDeal td
        WHERE td.site.id = :siteId
          AND td.deletedAt IS NULL
          AND td.status NOT IN ('CANCELLED', 'ENDED')
          AND td.checkIn < :checkOut AND td.checkOut > :checkIn
          AND (:excludeId IS NULL OR td.id <> :excludeId)
        """)
    int sumReservedQuantityByOverlappingDates(@Param("siteId") Long siteId,
                                              @Param("checkIn") LocalDate checkIn,
                                              @Param("checkOut") LocalDate checkOut,
                                              @Param("excludeId") Long excludeId);

    // ──────────────────────────────────────────────
    // 스케줄러용 벌크 UPDATE
    // ──────────────────────────────────────────────

    @Modifying
    @Query("""
        UPDATE TimeDeal td SET td.status = 'ACTIVE'
        WHERE td.status = 'SCHEDULED' AND td.saleStartAt <= :now AND td.saleEndAt > :now
        """)
    int activateScheduledDeals(@Param("now") LocalDateTime now);

    @Modifying
    @Query("""
        UPDATE TimeDeal td SET td.status = 'ENDED'
        WHERE td.status IN ('SCHEDULED', 'ACTIVE') AND td.saleEndAt <= :now
        """)
    int endExpiredDeals(@Param("now") LocalDateTime now);

    // ──────────────────────────────────────────────
    // 구매 전용 원자적 UPDATE
    // ──────────────────────────────────────────────

    /**
     * [원자적 soldCount 증가] - 구매 핵심 로직
     *
     * 기존 문제:
     *   T1: soldCount 읽기(3)  →  soldCount 쓰기(4)
     *   T2: soldCount 읽기(3)  →  soldCount 쓰기(4)  ← 동시 실행 시 둘 다 4로 저장 → 재고 초과
     *
     * 해결 방식 - DB에 원자적 연산을 위임:
     *   UPDATE time_deal
     *   SET sold_count = sold_count + :count
     *   WHERE id = :id
     *     AND status = 'ACTIVE'
     *     AND sold_count + :count <= quantity   ← 재고 초과 방지 조건
     *
     * DB는 이 UPDATE를 행 레벨 잠금과 함께 원자적으로 실행한다.
     * 동시에 두 트랜잭션이 들어와도 하나는 대기 → 순차 실행 → 재고 초과 불가.
     *
     * 반환값:
     *   1 → 구매 성공
     *   0 → 재고 부족 or 판매 중이 아님 (서비스에서 재조회 후 원인 판별)
     *
     * 왜 낙관적 락(@Version) 대신 원자적 UPDATE?
     *   - 타임딜은 플래시 세일 특성상 동시 구매 충돌이 매우 잦음
     *   - @Version + 재시도: 충돌 시 재시도가 폭주 → DB 부하 급증
     *   - 원자적 UPDATE: 락 없이도 DB가 직렬화를 보장 → 높은 처리량 유지
     *
     * 왜 비관적 락(SELECT FOR UPDATE) 대신?
     *   - SELECT FOR UPDATE는 행을 읽는 순간부터 커밋까지 잠금
     *   - 구매 트랜잭션이 짧아도 동시 요청이 모두 직렬 대기 → 처리량 병목
     *   - 원자적 UPDATE는 DB 내부에서만 직렬화 → 어플리케이션 레벨 대기 없음
     */
    @Modifying
    @Query("""
        UPDATE TimeDeal td
        SET td.soldCount = td.soldCount + :count
        WHERE td.id = :id
          AND td.status = :activeStatus
          AND (td.soldCount + :count) <= td.quantity
        """)
    int purchaseAtomically(
            @Param("id") Long id,
            @Param("count") int count,
            @Param("activeStatus") TimeDealStatus activeStatus
    );

    /**
     * soldCount가 quantity에 도달한 경우 SOLD_OUT으로 상태 변경
     *
     * purchaseAtomically() 성공 후 호출.
     * 이미 SOLD_OUT이거나 마지막 재고가 아닌 경우엔 0 rows 반환 (정상).
     */
    @Modifying
    @Query("""
        UPDATE TimeDeal td
        SET td.status = :soldOutStatus
        WHERE td.id = :id
          AND td.soldCount >= td.quantity
          AND td.status = :activeStatus
        """)
    int markSoldOutIfExhausted(
            @Param("id") Long id,
            @Param("soldOutStatus") TimeDealStatus soldOutStatus,
            @Param("activeStatus") TimeDealStatus activeStatus
    );
}