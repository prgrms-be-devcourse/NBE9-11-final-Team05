package com.back.ovengers.domain.user.dto;

import com.back.ovengers.domain.reservation.dto.MyReservationResponse;
import com.back.ovengers.domain.review.dto.MyReviewResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPageResponse {

    private Long id;
    private String nickname;
    private String phone;
    private String imageUrl;

    private List<MyReservationResponse> reservations;
    private List<MyReviewResponse> reviews;
}
