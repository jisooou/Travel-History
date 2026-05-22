package com.project.travel.place.dto.response;

import com.project.travel.place.entity.Place;
import com.project.travel.record.entity.Record;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PlaceResponseDto {
    private Integer placeNo;
    private Integer recordNo;
    private String placeName;
    private String placeAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String mapSource;
    private String mapPlaceId;
    private String imageUrl;

    public static PlaceResponseDto from(Place place) {
        return PlaceResponseDto.builder()
                .placeNo(place.getPlaceNo())
                .recordNo(place.getRecord().getRecordNo())
                .placeName(place.getPlaceName())
                .placeAddress(place.getPlaceAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .mapSource(place.getMapSource())
                .mapPlaceId(place.getMapPlaceId())
                .imageUrl(place.getImageUrl())
                .build();
    }
}
