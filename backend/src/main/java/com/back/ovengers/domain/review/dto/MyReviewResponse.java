package com.back.ovengers.domain.review.dto;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.review.entity.Review;

import java.time.LocalDate;

public record MyReviewResponse(
        Long reservationId,
        Long campingId,
        String campingName,
        String campingImageUrl,
        LocalDate checkIn,
        LocalDate checkOut,
        String reservationNumber,
        boolean hasReview,
        ReviewDetail review
) {
    public static MyReviewResponse from(Reservation reservation, Review review) {
        Camping camping = reservation.getSite().getCamping();
        return new MyReviewResponse(
                reservation.getId(),
                camping.getId(),
                camping.getName(),
                camping.getFirstImageUrl(),
                reservation.getCheckIn(),
                reservation.getCheckOut(),
                reservation.getRsvNum(),
                review != null,
                review != null ? ReviewDetail.from(review) : null
        );
    }
}
