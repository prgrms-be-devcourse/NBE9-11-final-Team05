package com.back.ovengers.domain.user.dto;

import com.back.ovengers.domain.reservation.dto.MyReservationResponse;
import com.back.ovengers.domain.review.dto.MyReviewResponse;
import com.back.ovengers.domain.user.entity.Role;
import lombok.Builder;

import java.util.List;

@Builder
public record MyPageResponse(

        Long id,
        String nickname,
        String phone,
        String imageUrl,
        Role role,

        List<MyReservationResponse> reservations,
        List<MyReviewResponse> reviews

) {
}
