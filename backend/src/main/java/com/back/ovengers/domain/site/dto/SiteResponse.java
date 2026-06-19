package com.back.ovengers.domain.site.dto;

import com.back.ovengers.domain.site.entity.Site;

public record SiteResponse(
        Long id,
        String name,
        String description,
        int baseCapacity,
        int maxCapacity,
        int totalCount,
        int price
) {

    public static SiteResponse from(Site site) {
        return new SiteResponse(
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
