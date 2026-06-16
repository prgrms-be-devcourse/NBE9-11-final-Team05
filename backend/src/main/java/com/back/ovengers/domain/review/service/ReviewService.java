package com.back.ovengers.domain.review.service;

import com.back.ovengers.domain.camping.repository.CampingRepository;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.reservation.entity.ReservationStatus;
import com.back.ovengers.domain.reservation.repository.ReservationRepository;
import com.back.ovengers.domain.review.dto.MyReviewResponse;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // 리뷰 먼저 조회
        Page<Review> reviews = reviewRepository.findByCampingIdWithUser(campingId, pageable);

        // 리뷰 없을 때만 캠핑장 존재 여부 확인
        if (reviews.isEmpty() && !campingRepository.existsByIdAndDeletedAtIsNull(campingId)) {
            throw new CustomException(ErrorCode.CAMPING_NOT_FOUND);
        }

        return reviews.map(ReviewResponse::from);
    }

    public void updateReview(User user, Long reviewId, ReviewRequest request) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 본인 리뷰인지 확인
        review.validateOwner(user.getId());

        // 더티 체킹
        review.update(request.getRating(), request.getContent());
    }

    public void deleteReview(User user, Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        review.validateOwner(user.getId());

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public Page<MyReviewResponse> getMyReviews(User user, Pageable pageable) {

        // 유저의 COMPLETED 예약 목록 조회 (Site, Camping fetch join)
        Page<Reservation> reservations = reservationRepository
                .findCompletedByUserId(user.getId(), ReservationStatus.COMPLETED, pageable);

        // 완료된 예약이 없으면 리뷰 조회 없이 바로 반환
        if (reservations.isEmpty()) {
            return Page.empty(pageable);
        }

        // 예약 ID 목록 추출
        List<Long> reservationIds = reservations.getContent().stream()
                .map(Reservation::getId)
                .toList();

        // 해당 예약들의 리뷰를 IN 쿼리로 한 번에 조회 (N+1 방지)
        Map<Long, Review> reviewMap = reviewRepository
                .findByReservationIdIn(reservationIds).stream()
                .collect(Collectors.toMap(
                        review -> review.getReservation().getId(),
                        review -> review
                ));

        // 예약 + 리뷰(있으면) 합쳐서 응답 조립
        return reservations.map(reservation ->
                MyReviewResponse.from(reservation, reviewMap.get(reservation.getId()))
        );
    }

}
