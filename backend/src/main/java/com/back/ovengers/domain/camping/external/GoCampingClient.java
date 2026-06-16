package com.back.ovengers.domain.camping.external;

import com.back.ovengers.domain.camping.external.dto.GoCampingApiImageItem;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiImageWrapper;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiItem;
import com.back.ovengers.domain.camping.external.dto.GoCampingApiWrapper;
import com.back.ovengers.global.exception.CustomException;
import com.back.ovengers.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoCampingClient {

    private final RestClient restClient;
    private final GoCampingProperties properties;

    public List<GoCampingApiItem> getCampList() {
        URI uri = buildUri(1);
        GoCampingApiWrapper response = fetchFromGoCamping(uri, GoCampingApiWrapper.class);

        int totalCount = response.totalCount();
        int pageSize = Integer.parseInt(properties.value().numOfRows());
        int totalPages = (totalCount + pageSize - 1) / pageSize;

        List<GoCampingApiItem> items = new ArrayList<>(response.items());

        for (int pageNo = 2; pageNo <= totalPages; pageNo++) {
            uri = buildUri(pageNo);
            response = fetchFromGoCamping(uri, GoCampingApiWrapper.class);
            items.addAll(response.items());
        }

        return items;
    }

    public List<GoCampingApiImageItem> getCampImageList(Long contentId) {
        URI uri = buildImageUri(1, contentId);
        GoCampingApiImageWrapper response = fetchFromGoCamping(uri, GoCampingApiImageWrapper.class);

        int totalCount = response.totalCount();
        int pageSize = Integer.parseInt(properties.value().numOfRows());
        int totalPages = (totalCount + pageSize - 1) / pageSize;

        List<GoCampingApiImageItem> items = new ArrayList<>(response.items());

        for (int pageNo = 2; pageNo <= totalPages; pageNo++) {
            uri = buildImageUri(pageNo, contentId);
            response = fetchFromGoCamping(uri, GoCampingApiImageWrapper.class);
            items.addAll(response.items());
        }

        return items;
    }

    private <T> T fetchFromGoCamping(URI uri, Class<T> responseType) {
        try {
            T result = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(responseType);

            if (result == null) {
                throw new CustomException(ErrorCode.GO_CAMPING_API_ERROR);
            }

            return result;
        } catch (RestClientException e) {
            String res = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);
            log.error("RESPONSE:" + res);
            log.error("ERROR:", e);
            throw new CustomException(ErrorCode.GO_CAMPING_API_ERROR);
        }
    }

    private URI buildUri(int pageNo) {
        return UriComponentsBuilder
                .fromUriString(properties.baseUrl() + properties.endpoint().basedList())
                .queryParam("serviceKey", properties.value().serviceKey())
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", properties.value().numOfRows())
                .queryParam("MobileOS", properties.value().mobileOS())
                .queryParam("MobileApp", properties.value().mobileApp())
                .queryParam("_type", properties.value().type())
                .build(true)
                .toUri();
    }

    private URI buildImageUri(int pageNo, Long contentId) {
        return UriComponentsBuilder
                .fromUriString(properties.baseUrl() + properties.endpoint().imageList())
                .queryParam("serviceKey", properties.value().serviceKey())
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", properties.value().numOfRows())
                .queryParam("MobileOS", properties.value().mobileOS())
                .queryParam("MobileApp", properties.value().mobileApp())
                .queryParam("_type", properties.value().type())
                .queryParam("contentId", contentId)
                .build(true)
                .toUri();
    }

}
