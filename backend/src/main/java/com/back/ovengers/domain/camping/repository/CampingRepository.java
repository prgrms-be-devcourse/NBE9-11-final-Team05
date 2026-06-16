package com.back.ovengers.domain.camping.repository;

import com.back.ovengers.domain.camping.entity.Camping;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
=======
import com.back.ovengers.domain.camping.entity.CampingStatus;
>>>>>>> 24ab3a7 (feat: 관리자 대시보드 조회 기능 구현 및 서비스 테스트 구현)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CampingRepository extends JpaRepository<Camping, Long> {

    List<Camping> findByHostIdAndDeletedAtIsNull(Long hostId);

    Optional<Camping> findByIdAndDeletedAtIsNull(Long campingId);

    // 소프트 딜리트 되지 않은 캠핑장 존재 여부 확인
    boolean existsByIdAndDeletedAtIsNull(Long id);

    @Query("""
        select c from Camping c 
            where c.deletedAt is null
                and (:keywordLike is null 
                or c.name like :keywordLike 
                or c.region like :keywordLike)
    """)
    Page<Camping> searchCamping(String keywordLike, Pageable pageable);

    // 관리자 대시보드
    long countByStatus(CampingStatus status);
}
