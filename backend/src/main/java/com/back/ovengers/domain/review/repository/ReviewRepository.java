package com.back.ovengers.domain.review.repository;

import com.back.ovengers.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 중복 리뷰 방지
    boolean existsByReservationId(Long reservationId);

    // 캠핑장 리뷰 목록 조회
    @Query("SELECT r FROM Review r " +
            "JOIN FETCH r.user u " +
            "WHERE r.camping.id = :campingId " +
            "AND r.camping.deletedAt IS NULL")
    Page<Review> findByCampingIdWithUser(
            @Param("campingId") Long campingId,
            Pageable pageable);
}
