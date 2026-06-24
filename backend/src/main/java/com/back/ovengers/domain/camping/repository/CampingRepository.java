package com.back.ovengers.domain.camping.repository;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
                and c.status = :status
                and (:keywordLike is null
                or c.name like :keywordLike
                or c.region like :keywordLike)
        """)
    Page<Camping> searchApprovedCamping(CampingStatus status, String keywordLike, Pageable pageable);

    // 관리자 대시보드
    long countByStatus(CampingStatus status);

    // 관리자 - 상태별 캠핑장 목록 조회 (호스트 정보 포함)
    // 현재는 승인 대기(PENDING) 목록 조회에 사용
    // Host LEFT JOIN FETCH로 호스트 탈퇴/없는 경우도 고려
    @Query(
            value = "SELECT c FROM Camping c LEFT JOIN FETCH c.host h " +
                    "WHERE c.status = :status",
            countQuery = "SELECT COUNT(c) FROM Camping c " +
                    "WHERE c.status = :status"
    )
    Page<Camping> findByStatusWithHost(@Param("status") CampingStatus status, Pageable pageable);

    // IN 쿼리로 한 번에 가져와서 쿼리 1번에 완료
    List<Camping> findByIdIn(List<Long> campingIds);

    @Query("""
        SELECT c
        FROM Camping c
        WHERE c.deletedAt IS NULL
          AND c.host IS NULL
          AND c.contentId IS NOT NULL
          AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    List<Camping> searchClaimableCampings(@Param("keyword") String keyword);
}
