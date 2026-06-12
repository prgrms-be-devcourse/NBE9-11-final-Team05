package com.back.ovengers.domain.auth.dto;

public record LoginResponse(
        String accessToken,
        String role
) {
}