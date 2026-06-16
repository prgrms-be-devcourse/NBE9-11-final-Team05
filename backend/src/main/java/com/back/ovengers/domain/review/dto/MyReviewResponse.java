package com.back.ovengers.domain.review.dto;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.reservation.entity.Reservation;
import com.back.ovengers.domain.review.entity.Review;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MyReviewResponse {
    private Long reservationId;
    private Long campingId;
    private String campingName;
    private String campingImageUrl;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String reservationNumber;
    private boolean hasReview;
    private ReviewDetail review;

    private MyReviewResponse(Reservation reservation, Review review) {
        Camping camping = reservation.getSite().getCamping();
        this.reservationId = reservation.getId();
        this.campingId = camping.getId();
        this.campingName = camping.getName();
        this.campingImageUrl = camping.getFirstImageUrl();
        this.checkIn = reservation.getCheckIn();
        this.checkOut = reservation.getCheckOut();
        this.reservationNumber = reservation.getRsvNum();
        this.hasReview = review != null;
        this.review = review != null ? ReviewDetail.from(review) : null;
    }

    public static MyReviewResponse from(Reservation reservation, Review review){
        return new MyReviewResponse(reservation, review);
    }
}
