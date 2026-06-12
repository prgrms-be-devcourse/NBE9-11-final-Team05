package com.back.ovengers.domain.camping.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "go-camping")
public record GoCampingProperties(
        String baseUrl,
        String serviceKey
) {}
