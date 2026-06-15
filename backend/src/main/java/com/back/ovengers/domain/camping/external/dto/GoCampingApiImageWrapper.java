package com.back.ovengers.domain.camping.external.dto;

import java.util.List;

public record GoCampingApiImageWrapper(
        Response response
) {
    public int totalCount() {
        return response().body().totalCount();
    }

    public List<GoCampingApiImageItem> items() {
        if (response == null || response.body() == null || response.body().items() == null) {
            return List.of();
        }

        List<GoCampingApiImageItem> item = response.body().items().item();
        return item == null ? List.of() : item;
    }

    public record Response(
            Body body
    ) {}

    public record Body(
            Items items,
            int totalCount
    ) {}

    public record Items(
            List<GoCampingApiImageItem> item
    ) {}
}
