package com.back.ovengers.domain.review.service;

import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.review.dto.ReviewRequest;
import com.back.ovengers.domain.review.entity.Review;
import com.back.ovengers.domain.review.repository.ReviewRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void createReview(User user, Long reservationId, ReviewRequest request){

        // 예약 존재 확인
        Reservation reservation = reservationRepository.findByIdWithSiteAndCamping(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // 본인 확인 및 이용 완료 여부 검증
        reservation.validateReviewAuthority(user.getId());

        // 중복 리뷰 확인
        if (reviewRepository.existsByReservationId(reservationId)) {
            throw new CustomException(ErrorCode.ALREADY_REVIEWED);
        }

        // 빌더로 객체 생성 및 저장
        Review review = Review.builder()
                .user(user)
                .camping(reservation.getSite().getCamping())
                .reservation(reservation)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        reviewRepository.save(review);
    }
}
