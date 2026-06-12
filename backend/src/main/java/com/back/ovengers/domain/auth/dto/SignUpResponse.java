package com.back.ovengers.domain.auth.dto;

import lombok.Builder;

@Builder
public record SignUpResponse(
        Long id,
        String email,
        String nickname,
        String role
) {
}