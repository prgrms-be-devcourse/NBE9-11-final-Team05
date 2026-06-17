package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.site.entity.Site;

public record CampingSiteResponse(
        String name,
        String description,
        int baseCapacity,
        int maxCapacity,
        int totalCount,
        int price
) {

    public static CampingSiteResponse from(Site site) {
        return new CampingSiteResponse(
                site.getName(),
                site.getDescription(),
                site.getBaseCapacity(),
                site.getMaxCapacity(),
                site.getTotalAmount(),
                site.getPrice()
        );
    }


}
