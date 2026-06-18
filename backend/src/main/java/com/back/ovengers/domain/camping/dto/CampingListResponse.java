package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.camping.entity.Camping;

import java.time.LocalDateTime;

public record CampingListResponse(
        Long id,
        String name,
        String region,
        String city,
        String address,
        String firstImageUrl,
        Float rating,
        LocalDateTime createdAt
) {

    public static CampingListResponse from(Camping camping) {
        return new CampingListResponse(
                camping.getId(),
                camping.getName(),
                camping.getRegion(),
                camping.getCity(),
                camping.getAddress(),
                camping.getFirstImageUrl(),
                camping.getRating(),
                camping.getCreatedAt()
        );
    }

}
