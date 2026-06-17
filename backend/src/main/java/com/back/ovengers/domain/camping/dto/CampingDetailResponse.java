package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.camping.entity.Camping;
import com.back.ovengers.domain.site.dto.SiteResponse;

import java.time.LocalTime;
import java.util.List;

public record CampingDetailResponse(
        Long id,
        String firstImageUrl,
        String name,
        String homepage,
        String address,
        Float rating,
        String description,
        String phone,
        LocalTime checkInTime,
        LocalTime checkOutTime,
        String notice,
        List<String> imageUrls,
        List<SiteResponse> sites
) {

    public static CampingDetailResponse from(
            Camping camp,
            List<String> imageUrls,
            List<SiteResponse> sites
    ) {
        return new CampingDetailResponse(
                camp.getId(),
                camp.getFirstImageUrl(),
                camp.getName(),
                camp.getHomepage(),
                camp.getAddress(),
                camp.getRating(),
                camp.getDescription(),
                camp.getPhone(),
                camp.getCheckInTime(),
                camp.getCheckOutTime(),
                camp.getNotice(),
                imageUrls,
                sites
        );
    }

}
