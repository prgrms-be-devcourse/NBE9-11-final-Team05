package com.back.ovengers.domain.site.dto;

import com.back.ovengers.domain.site.entity.Site;

public record SiteCreateResponse(
        Long id,
        String name,
        String description,
        Integer baseCapacity,
        Integer maxCapacity,
        Integer totalAmount,
        Integer price
) {
    public static SiteCreateResponse from(Site site) {
        return new SiteCreateResponse(
                site.getId(),
                site.getName(),
                site.getDescription(),
                site.getBaseCapacity(),
                site.getMaxCapacity(),
                site.getTotalAmount(),
                site.getPrice()
        );
    }
}
