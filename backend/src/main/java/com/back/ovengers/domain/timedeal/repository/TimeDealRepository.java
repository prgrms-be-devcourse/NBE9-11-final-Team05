package com.back.ovengers.domain.timedeal.repository;

import com.back.ovengers.domain.timedeal.entity.TimeDeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TimeDealRepository extends JpaRepository<TimeDeal, Long> {

    /**
     * 삭제되지 않은 타임딜 단건 조회
     *
     * TimeDeal → Site → Camping 정보를 Fetch Join으로 함께 조회하여
     * 연관 엔티티 접근 시 발생하는 N+1 문제를 방지한다.
     *
     * @param id 타임딜 ID
     * @return 타임딜 정보
     */
    @Query("""
        SELECT td FROM TimeDeal td
        JOIN FETCH td.site s
        JOIN FETCH s.camping c
        WHERE td.id = :id AND td.deletedAt IS NULL
        """)
    Optional<TimeDeal> findActiveById(@Param("id") Long id);

    /**
     * 특정 호스트가 등록한 타임딜 목록 조회
     *
     * Soft Delete 되지 않은 타임딜만 조회하며,
     * 최신 등록 순(createdAt DESC)으로 정렬한다.
     *
     * @param hostId 호스트 ID
     * @param pageable 페이징 정보
     * @return 호스트의 타임딜 목록
     */
    @Query("""
        SELECT td FROM TimeDeal td
        WHERE td.site.camping.host.id = :hostId
          AND td.deletedAt IS NULL
        ORDER BY td.createdAt DESC
        """)
    Page<TimeDeal> findAllByHostId(@Param("hostId") Long hostId, Pageable pageable);


    /**
     * 현재 판매 중인 타임딜 목록 조회
     *
     * ACTIVE 상태이면서 판매 시작 시간이 현재 이전이고,
     * 판매 종료 시간이 현재 이후인 타임딜만 조회한다.
     *
     * @param now 현재 시간
     * @param pageable 페이징 정보
     * @return 판매 중인 타임딜 목록
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
     * 동일 Site의 동일 기간에 이미 할당된 타임딜 수량 합계 조회
     *
     * 타임딜 등록/수정 시 재고 초과 여부를 검증하기 위해 사용한다.
     *
     * 예)
     * Site 총 재고 : 10
     * 타임딜 A : 3개
     * 타임딜 B : 2개
     *
     * 결과 : 5 반환
     *
     * 수정 시에는 현재 수정 중인 타임딜을 제외하기 위해
     * excludeId를 사용한다.
     *
     * @param siteId Site ID
     * @param checkIn 체크인 날짜
     * @param checkOut 체크아웃 날짜
     * @param excludeId 수정 중인 타임딜 ID (신규 등록 시 null)
     * @return 이미 할당된 타임딜 수량 합계
     */
    // 같은 site, 겹치는 날짜에 이미 다른 타임딜로 잡혀있는 수량 합
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


    /**
     * 판매 시작 시간이 된 타임딜을 ACTIVE 상태로 변경
     *
     * SCHEDULED 상태이며,
     * saleStartAt <= 현재시간 < saleEndAt 조건을 만족하는
     * 타임딜들을 일괄 활성화한다.
     *
     * 스케줄러에서 주기적으로 호출한다.
     *
     * @param now 현재 시간
     * @return 상태가 변경된 타임딜 개수
     */
    @Modifying
    @Query("""
        UPDATE TimeDeal td SET td.status = 'ACTIVE'
        WHERE td.status = 'SCHEDULED' AND td.saleStartAt <= :now AND td.saleEndAt > :now
        """)
    int activateScheduledDeals(@Param("now") LocalDateTime now);


    /**
     * 판매 종료 시간이 지난 타임딜을 ENDED 상태로 변경
     *
     * ACTIVE 또는 SCHEDULED 상태의 타임딜 중
     * saleEndAt <= 현재시간 조건을 만족하는 데이터를
     * 일괄 종료 처리한다.
     *
     * 스케줄러에서 주기적으로 호출한다.
     *
     * @param now 현재 시간
     * @return 상태가 변경된 타임딜 개수
     */
    @Modifying
    @Query("""
        UPDATE TimeDeal td SET td.status = 'ENDED'
        WHERE td.status IN ('SCHEDULED', 'ACTIVE') AND td.saleEndAt <= :now
        """)
    int endExpiredDeals(@Param("now") LocalDateTime now);
}
