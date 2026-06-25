package com.back.ovengers.domain.admin.dto;

import com.back.ovengers.domain.user.entity.User;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long userId,
        String email,
        String name,
        String nickname,
        String role,
        String status,
        boolean isDeleted,
        LocalDateTime createdAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getDeletedAt() != null,
                user.getCreatedAt()
        );
    }
}
