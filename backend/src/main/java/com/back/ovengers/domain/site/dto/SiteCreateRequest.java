package com.back.ovengers.domain.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SiteCreateRequest(

        @NotBlank(message = "구역명은 필수입니다.")
        String name,

        String description,

        @NotNull(message = "기준 인원은 필수입니다.")
        @Positive(message = "기준 인원은 1명 이상이어야 합니다.")
        Integer baseCapacity,

        @NotNull(message = "최대 인원은 필수입니다.")
        @Positive(message = "최대 인원은 1명 이상이어야 합니다.")
        Integer maxCapacity,

        @NotNull(message = "구역 수는 필수입니다.")
        @Positive(message = "구역 수는 1개 이상이어야 합니다.")
        Integer totalAmount,

        @NotNull(message = "가격은 필수입니다.")
        @Positive(message = "가격은 0원보다 커야 합니다.")
        Integer price

) {
}
