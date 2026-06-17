package com.back.ovengers.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record CampingRejectRequest(
        @NotBlank(message = "거절 사유는 필수입니다.")
        String reason
) {
}
