package com.back.ovengers.domain.camping.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "go-camping")
public record GoCampingProperties(
        String baseUrl,
        Endpoint endpoint,
        Value value
) {

    public record Endpoint(
        String basedList,
        String imageList
    ) { }

    public record Value(
            String numOfRows,
            String mobileOS,
            String mobileApp,
            String serviceKey,
            String type
    ) { }

}
