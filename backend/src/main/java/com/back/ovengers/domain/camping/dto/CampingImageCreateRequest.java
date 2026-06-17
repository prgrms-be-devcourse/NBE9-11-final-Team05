package com.back.ovengers.domain.camping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CampingImageCreateRequest(
        @NotBlank String imageUrl
) {
}
