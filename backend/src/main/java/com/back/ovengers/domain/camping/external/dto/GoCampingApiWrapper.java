package com.back.ovengers.domain.camping.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoCampingApiWrapper(
        Response response
) {
    public int totalCount() {
        return response().body().totalCount();
    }

    public List<GoCampingApiItem> items() {
        if (response == null || response.body() == null || response.body().items() == null) {
            return List.of();
        }

        List<GoCampingApiItem> item = response.body().items().item();
        return item == null ? List.of() : item;
    }

    public record Response(
            Body body
    ) { }

    public record Body(
            Items items,
            int totalCount
    ) { }

    public record Items(
            List<GoCampingApiItem> item
    ) { }
}
