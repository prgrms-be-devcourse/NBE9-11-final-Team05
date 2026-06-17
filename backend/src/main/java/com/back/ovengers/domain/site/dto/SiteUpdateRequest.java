package com.back.ovengers.domain.site.dto;

import jakarta.validation.constraints.Positive;

public record SiteUpdateRequest(
        String name,
        String description,
        @Positive(message = "기준 인원은 1명 이상이어야 합니다.")
        Integer baseCapacity,
        @Positive(message = "최대 인원은 1명 이상이어야 합니다.")
        Integer maxCapacity,
        @Positive(message = "구역 수는 1개 이상이어야 합니다.")
        Integer totalAmount,
        @Positive(message = "가격은 0원보다 커야 합니다.")
        Integer price
) {
}
