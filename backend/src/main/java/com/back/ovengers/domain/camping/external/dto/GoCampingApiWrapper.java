package com.back.ovengers.domain.camping.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoCampingApiWrapper(
        Response response
) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public int totalCount() {
        if (response == null || response.body() == null) {
            return 0;
        }

        return response().body().totalCount();
    }

    public List<GoCampingApiItem> items() {
        if (totalCount() == 0) {
            return List.of();
        }

        JsonNode item = response.body().items().path("item");

        return objectMapper.convertValue(
                item,
                new TypeReference<>() {}
        );
    }

    public record Response(
            Body body
    ) { }

    public record Body(
            JsonNode items,
            int totalCount
    ) { }
}
