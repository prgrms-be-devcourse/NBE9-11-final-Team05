package com.back.ovengers.domain.camping.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public record CampingUpdateRequest(
        String firstImageUrl,
        String name,
        String homepage,
        String region,
        String city,
        String address,
        String description,
        String phone,
        LocalTime checkInTime,
        LocalTime checkOutTime,
        String notice,
        BigDecimal lat,
        BigDecimal lng
) {
}