package com.back.ovengers.domain.user.dto;

import com.back.ovengers.domain.reservation.dto.MyReservationResponse;
import com.back.ovengers.domain.review.dto.MyReviewResponse;
import com.back.ovengers.domain.user.entity.Role;
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
    private Role role;

    private List<MyReservationResponse> reservations;
    private List<MyReviewResponse> reviews;
}
