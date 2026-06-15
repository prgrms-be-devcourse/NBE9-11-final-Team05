package com.back.ovengers.domain.review.repository;

import com.back.ovengers.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 중복 리뷰 방지
    boolean existsByReservationId(Long reservationId);

    // 캠핑장 리뷰 목록 조회
    List<Review> findByCampingId(Long campingId);


}
