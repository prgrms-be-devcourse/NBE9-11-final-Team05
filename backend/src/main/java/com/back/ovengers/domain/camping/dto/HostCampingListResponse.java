package com.back.ovengers.domain.camping.dto;

public record HostCampingListResponse(
        Long campingId,
        String name,
        String region,
        String city,
        String address,
        String firstImageUrl,
        Float rating
) {
}
