package com.back.ovengers.domain.site.repository;

import com.back.ovengers.domain.site.entity.Site;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface SiteRepository extends JpaRepository<Site, Long> {

    // 사이트 조회 비관적 락 적용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Site s WHERE s.id = :id")
    Optional<Site> findByIdWithLock(@Param("id") Long id);

    Optional<Site> findByIdAndDeletedAtIsNull(Long siteId);

    boolean existsByCampingIdAndNameAndDeletedAtIsNull(
            Long campingId,
            String name
    );

    boolean existsByCampingIdAndNameAndIdNotAndDeletedAtIsNull(
            Long campingId,
            String name,
            Long siteId
    );
}
