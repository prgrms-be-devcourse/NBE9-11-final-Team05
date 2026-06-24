package com.back.ovengers.domain.camping.event;

public record CampingApprovedEvent(
        Long campingId,
        String campingName
)
{}
