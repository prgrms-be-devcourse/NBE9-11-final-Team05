package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.camping.entity.CampingStatus;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record HostCampingDetailResponse(
        Long id,
        String firstImageUrl,
        String tourNum,
        String businessNum,
        CampingStatus status,
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
        Float rating,
        List<HostCampingImageResponse> images
) {
    public static HostCampingDetailResponse from(
            Camping camping,
            List<HostCampingImageResponse> images
    ) {
        return new HostCampingDetailResponse(
                camping.getId(),
                camping.getFirstImageUrl(),
                camping.getTourNum(),
                camping.getBusinessNum(),
                camping.getStatus(),
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
                camping.getRating(),
                images
        );
    }
}
