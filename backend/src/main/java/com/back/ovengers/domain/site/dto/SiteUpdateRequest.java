package com.back.ovengers.domain.site.dto;

public record SiteUpdateRequest(
        String name,
        String description,
        Integer baseCapacity,
        Integer maxCapacity,
        Integer totalAmount,
        Integer price
) {
}
