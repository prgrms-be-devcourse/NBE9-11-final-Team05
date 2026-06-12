package com.back.ovengers.domain.camping.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GoCampingClient {

    private final RestClient restClient;
    private final GoCampingProperties properties;
    private final ObjectMapper objectMapper;
    private static final int NUM_OF_ROWS = 1000;

    public List<GoCampingApiResponse> getCampList() {
        JsonNode response = requestCampList(1);
        int totalCount = response
                .path("response")
                .path("body")
                .path("totalCount")
                .asInt();

        int totalPages = (int) Math.ceil((double) totalCount / NUM_OF_ROWS);

        List<GoCampingApiResponse> result = extractItems(response);

        for (int pageNo = 2; pageNo <= totalPages; pageNo++) {
            response = requestCampList(pageNo);
            result.addAll(extractItems(response));
        }

        return result;
    }

    private JsonNode requestCampList(int pageNo) {
        URI uri = UriComponentsBuilder
                .fromUriString(properties.baseUrl() + "/basedList")
                .queryParam("serviceKey", properties.serviceKey())
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", NUM_OF_ROWS)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "OVENGERS")
                .queryParam("_type", "json")
                .build(true)
                .toUri();

        return restClient.get()
                        .uri(uri)
                        .retrieve()
                        .body(JsonNode.class);
    }

    private List<GoCampingApiResponse> extractItems(JsonNode response) {
        if (response == null) {
            return List.of();
        }

        JsonNode item = response
                .path("response")
                .path("body")
                .path("items")
                .path("item");

        return objectMapper.convertValue(
                item,
                new TypeReference<>() {}
        );
    }
}
