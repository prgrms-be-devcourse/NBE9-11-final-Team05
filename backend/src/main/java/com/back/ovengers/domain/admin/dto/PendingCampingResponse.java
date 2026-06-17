package com.back.ovengers.domain.admin.dto;

import com.back.ovengers.domain.camping.entity.Camping;

import java.time.LocalDate;

public record PendingCampingResponse(
        Long campingId,
        String campingName,
        String hostName,
        LocalDate createdAt,
        String businessNum
) {
    public static PendingCampingResponse from(Camping camping) {
        return new PendingCampingResponse(
                camping.getId(),
                camping.getName(),
                camping.getHost() != null ? camping.getHost().getName() : null,
                camping.getCreatedAt().toLocalDate(),
                camping.getBusinessNum()
        );
    }
}
