package com.project.travel.place.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PlaceSearchResponseDto {
    private String placeName;
    private String placeAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String mapSource;
    private String mapPlaceId;
}
