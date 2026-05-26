package com.project.travel.place.dto.response;

import lombok.Getter;

import java.util.List;

//외부 Map API를 활용하는 코드 : Kakao Map API
@Getter
public class MapPlaceSearchResponseDto {
    private List<Document> documents;

    @Getter
    public static class Document {
        private String id;
        private String place_name;
        private String address_name;
        private String road_address_name;
        private String x;
        private String y;
    }
}
