package com.back.ovengers.domain.site.repository;

import com.back.ovengers.domain.site.entity.Site;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface SiteRepository extends JpaRepository<Site, Long> {

    // 사이트 조회 비관적 락 적용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Site s WHERE s.id = :id")
    Optional<Site> findByIdWithLock(@Param("id") Long id);

    Optional<Site> findByIdAndDeletedAtIsNull(Long siteId);

    List<Site> findByCampingId(Long campingId);

    List<Site> findByCampingIdAndDeletedAtIsNullOrderByIdAsc(Long campingId);

    boolean existsByCampingIdAndNameAndDeletedAtIsNull(
            Long campingId,
            String name
    );

    boolean existsByCampingIdAndNameAndIdNotAndDeletedAtIsNull(
            Long campingId,
            String name,
            Long siteId
    );

    @Modifying
    @Query("""
    update Site s
    set s.deletedAt = CURRENT_TIMESTAMP
    where s.camping.id = :campingId
      and s.deletedAt is null
    """)
    void softDeleteByCampingId(Long campingId);

    // guestCount 조건이 있으면 인원 조건을 만족하는 Site 중 최저가 (검색 결과와 일치)
// guestCount 없으면 전체 Site 최저가
// Object[0] = camping_id, Object[1] = min_price
    @Query("SELECT s.camping.id, MIN(s.price) FROM Site s " +
            "WHERE s.camping.id IN :campingIds " +
            "AND s.deletedAt IS NULL " +
            "AND (:guestCount IS NULL OR s.maxCapacity >= :guestCount) " +
            "GROUP BY s.camping.id")
    List<Object[]> findMinPriceByCampingIds(
            @Param("campingIds") List<Long> campingIds,
            @Param("guestCount") Integer guestCount);
}
