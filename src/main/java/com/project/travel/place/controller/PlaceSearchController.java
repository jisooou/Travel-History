package com.project.travel.place.controller;

import com.project.travel.global.response.ApiResponse;
import com.project.travel.place.dto.response.PlaceSearchResponseDto;
import com.project.travel.place.service.PlaceSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/place")
public class PlaceSearchController {
    private final PlaceSearchService placeSearchService;

    @GetMapping("/search")
    public ApiResponse<List<PlaceSearchResponseDto>> searchPlace(
            @RequestParam String keyword
    ) {
        return ApiResponse.success(placeSearchService.searchPlace(keyword));
    }
}
