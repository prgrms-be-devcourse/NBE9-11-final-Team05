package com.back.ovengers.domain.review.service;

import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.review.dto.ReviewRequest;
import com.back.ovengers.domain.review.dto.ReviewResponse;
import com.back.ovengers.domain.review.entity.Review;
import com.back.ovengers.domain.review.repository.ReviewRepository;
import com.back.ovengers.domain.user.entity.User;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final CampingRepository campingRepository;

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

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getCampingReviews(Long campingId, Pageable pageable) {

        // 삭제된 캠핑장 포함 존재 여부 확인
        if (!campingRepository.existsByIdAndDeletedAtIsNull(campingId)) {
            throw new CustomException(ErrorCode.CAMPING_NOT_FOUND);
        }

        // 리뷰 목록 조회 (N+1 방지, 삭제된 캠핑장 제외)
        return reviewRepository.findByCampingIdWithUser(campingId, pageable)
                .map(ReviewResponse::from);
    }

}
