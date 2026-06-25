package com.back.ovengers.domain.review.repository;

import com.back.ovengers.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 중복 리뷰 방지
    boolean existsByReservationId(Long reservationId);

    // 캠핑장 리뷰 목록 조회
    @Query(
            // 리뷰 + 유저 정보 한 번에 조회 (N+1 방지)
            value = "SELECT r FROM Review r LEFT JOIN FETCH r.user u " +
                    "WHERE r.camping.id = :campingId ",

            // Page 반환 시 Spring Data JPA가 자동으로 카운트 쿼리 실행
            // totalElements, totalPages 계산에 사용
            // 개수만 세면 되므로 JOIN 불필요 → 성능 최적화
            countQuery = "SELECT COUNT(r) FROM Review r " +
                    "WHERE r.camping.id = :campingId"
    )
    Page<Review> findByCampingIdWithUser(
            @Param("campingId") Long campingId,
            Pageable pageable);

    // 아이디 목록에 포함되는 리뷰들 리스트로 가져옴
    List<Review> findByReservationIdIn(List<Long> reservationIds);

    @Query("SELECT r FROM Review r JOIN FETCH r.reservation res JOIN FETCH res.site s JOIN FETCH s.camping c WHERE r.user.id = :userId")
    List<Review> findAllByUserId(@Param("userId") Long userId);
}
