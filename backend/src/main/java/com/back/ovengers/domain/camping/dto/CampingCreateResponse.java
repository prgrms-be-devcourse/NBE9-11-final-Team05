package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.camping.entity.CampingStatus;

public record CampingCreateResponse (
    Long id,
    String name,
    CampingStatus status
) {}
