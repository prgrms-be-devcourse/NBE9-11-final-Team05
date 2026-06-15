package com.back.ovengers.domain.camping.dto;

import com.back.ovengers.domain.camping.entity.CampingStatus;

public record CampingCreateResponse (
    Long id,
    CampingStatus status
) {}
