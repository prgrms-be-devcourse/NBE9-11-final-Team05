package com.back.ovengers.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageResponse {

    private Long id;
    private String nickname;
    private String phone;
    private String imageUrl;

    // 리뷰와, 예약이 구현되면 추후 추가
//    private List<MyReservationResponse> reservations;
//    private List<MyReviewResponse> reviews;
}
