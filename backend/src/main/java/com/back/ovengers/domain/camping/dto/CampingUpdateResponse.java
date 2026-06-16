package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.camping.entity.Camping;

import java.math.BigDecimal;
import java.time.LocalTime;

public record CampingUpdateResponse(
        Long id,
        String firstImageUrl,
        String name,
        String region,
        String city,
        String address,
        String description,
        String notice,
        String phone,
        String homepage,
        LocalTime checkInTime,
        LocalTime checkOutTime,
        BigDecimal lat,
        BigDecimal lng,
        Float rating
) {
    public static CampingUpdateResponse from(Camping camping) {
        return new CampingUpdateResponse(
                camping.getId(),
                camping.getFirstImageUrl(),
                camping.getName(),
                camping.getRegion(),
                camping.getCity(),
                camping.getAddress(),
                camping.getDescription(),
                camping.getNotice(),
                camping.getPhone(),
                camping.getHomepage(),
                camping.getCheckInTime(),
                camping.getCheckOutTime(),
                camping.getLat(),
                camping.getLng(),
                camping.getRating()
        );
    }
}
