package com.back.ovengers.domain.camping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CampingClaimRequest(
        @NotNull Long contentId,
        @NotBlank String tourNum
) {
}
