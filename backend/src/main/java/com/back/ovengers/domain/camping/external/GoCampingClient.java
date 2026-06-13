package com.back.ovengers.domain.camping.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GoCampingClient {

    private final RestClient restClient;
    private final GoCampingProperties properties;

    public List<GoCampingApiItem> getCampList() {
        GoCampingApiWrapper response = requestCampList(1);

        int totalCount = response.totalCount();
        int pageSize = Integer.parseInt(properties.value().numOfRows());
        int totalPages = (totalCount + pageSize - 1) / pageSize;

        List<GoCampingApiItem> items = new ArrayList<>(response.items());

        for (int pageNo = 2; pageNo <= totalPages; pageNo++) {
            items.addAll(requestCampList(pageNo).items());
        }

        return items;
    }

    private GoCampingApiWrapper requestCampList(int pageNo) {
        URI uri = UriComponentsBuilder
                .fromUriString(properties.baseUrl() + properties.endpoint().basedList())
                .queryParam("serviceKey", properties.value().serviceKey())
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", properties.value().numOfRows())
                .queryParam("MobileOS", properties.value().mobileOS())
                .queryParam("MobileApp", properties.value().mobileApp())
                .queryParam("_type", properties.value().type())
                .build(true)
                .toUri();

        GoCampingApiWrapper result = restClient.get()
                                        .uri(uri)
                                        .retrieve()
                                        .body(GoCampingApiWrapper.class);

        // TODO: 예외처리
        if (result == null) {
            throw new IllegalStateException("캠핑장 정보 조회 실패");
        }

        return result;
    }
}
