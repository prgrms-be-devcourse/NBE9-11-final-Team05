package com.back.ovengers.domain.user.dto;

public record UserUpdateRequest(

        String nickname,
        String phone,
        String imageUrl

) {
}
