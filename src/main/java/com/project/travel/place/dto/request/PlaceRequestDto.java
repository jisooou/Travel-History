package com.project.travel.place.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PlaceRequestDto {
    @NotBlank
    private String placeName;

    private String placeAddress;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;

    private String mapSource;

    private String mapPlaceId;

    private String imageUrl;
}
