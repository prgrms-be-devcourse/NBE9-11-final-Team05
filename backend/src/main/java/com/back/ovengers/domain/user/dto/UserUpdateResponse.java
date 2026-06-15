package com.back.ovengers.domain.user.dto;

import java.time.LocalDateTime;


public record UserUpdateResponse(
        Long id,
        String name,
        String nickname,
        String phone,
        String imageUrl,
        LocalDateTime createdAt
) {
}
