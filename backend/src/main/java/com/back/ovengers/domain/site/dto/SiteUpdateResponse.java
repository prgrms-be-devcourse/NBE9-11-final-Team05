package com.back.ovengers.domain.site.dto;

import com.back.ovengers.domain.site.entity.Site;

public record SiteUpdateResponse(
        Long id,
        String name,
        String description,
        Integer baseCapacity,
        Integer maxCapacity,
        Integer totalAmount,
        Integer price
) {
    public static SiteUpdateResponse from(Site site) {
        return new SiteUpdateResponse(
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
