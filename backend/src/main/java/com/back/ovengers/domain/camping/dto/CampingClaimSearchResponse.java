package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.camping.entity.Camping;

public record CampingClaimSearchResponse(
        Long campingId,
        String name,
        String region,
        String city,
        String address,
        String firstImageUrl
) {
    public static CampingClaimSearchResponse from(Camping camping) {
        return new CampingClaimSearchResponse(
                camping.getId(),
                camping.getName(),
                camping.getRegion(),
                camping.getCity(),
                camping.getAddress(),
                camping.getFirstImageUrl()
        );
    }
}
