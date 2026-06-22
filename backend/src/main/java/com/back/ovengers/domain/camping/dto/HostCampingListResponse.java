package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;

public record HostCampingListResponse(
        Long id,
        String name,
        String region,
        String city,
        String address,
        String firstImageUrl,
        Float rating,
        CampingStatus status
) {
    public static HostCampingListResponse from(Camping camping) {
        return new HostCampingListResponse(
                camping.getId(),
                camping.getName(),
                camping.getRegion(),
                camping.getCity(),
                camping.getAddress(),
                camping.getFirstImageUrl(),
                camping.getRating(),
                camping.getStatus()
        );
    }
}
