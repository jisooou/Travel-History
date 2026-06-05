package com.project.travel.place.service;

import com.project.travel.place.dto.response.MapApiPlaceSearchResponseDto;
import com.project.travel.place.dto.response.PlaceSearchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {
    private final RestClient restClient = RestClient.create();

    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;

    public List<PlaceSearchResponseDto> searchPlace(String keyword) {
        MapApiPlaceSearchResponseDto responseDto = restClient.get()
                .uri(
                        uriBuilder -> uriBuilder
                                .scheme("https")
                                .host("dapi.kakao.com")
                                .path("/v2/local/search/keyword.json")
                                .queryParam("query", keyword)
                                .build()
                )
                .header("Authorization", "KakaoAK " + kakaoRestApiKey)
                .retrieve()
                .body(MapApiPlaceSearchResponseDto.class);

        if (responseDto == null || responseDto.getDocuments() == null) {
            return List.of();
        }

        return responseDto.getDocuments().stream()
                .map(document -> PlaceSearchResponseDto.builder()
                        .placeName(document.getPlace_name())
                        .placeAddress(
                                document.getRoad_address_name() != null && !document.getRoad_address_name().isBlank()
                                        ? document.getRoad_address_name()
                                        : document.getAddress_name()
                        )
                        .latitude(new BigDecimal(document.getY()))
                        .longitude(new BigDecimal(document.getX()))
                        .mapSource("KAKAO")
                        .mapPlaceId(document.getId())
                        .build()
                )
                .toList();
    }
}
