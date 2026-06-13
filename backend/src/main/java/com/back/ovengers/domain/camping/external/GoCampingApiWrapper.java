package com.back.ovengers.domain.camping.external;

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
        return response().body().items().item();
    }

    public record Response(
            Body body
    ) {}

    public record Body(
            Items items,
            int totalCount
    ) {}

    public record Items(
            List<GoCampingApiItem> item
    ) {}
}
