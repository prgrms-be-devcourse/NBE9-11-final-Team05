package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.camping.entity.CampingImage;

public record HostCampingImageResponse(
        Long imageId,
        String imageUrl,
        boolean thumbnail
) {
    public static HostCampingImageResponse from(
            CampingImage image,
            String firstImageUrl
    ) {
        return new HostCampingImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getImageUrl().equals(firstImageUrl)
        );
    }
}
