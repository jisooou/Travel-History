package com.project.travel.place.controller;

import com.project.travel.auth.security.CustomUserDetails;
import com.project.travel.global.response.ApiResponse;
import com.project.travel.place.dto.request.PlaceRequestDto;
import com.project.travel.place.dto.response.PlaceResponseDto;
import com.project.travel.place.service.PlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/place")
public class PlaceController {
    private final PlaceService placeService;

    @PostMapping("/records/{recordNo}")
    public ApiResponse<PlaceResponseDto> addPlaceToRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer recordNo,
            @Valid @RequestBody PlaceRequestDto requestDto
    ) {
        return ApiResponse.success(placeService.addPlaceToRecord(userDetails.getUserNo(), recordNo, requestDto));
    }

    //    회원
    @GetMapping("/records/{recordNo}")
    public ApiResponse<List<PlaceResponseDto>> getUserPlaceOfRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer recordNo
    ) {
        return ApiResponse.success(placeService.getUserPlaceOfRecord(userDetails.getUserNo(), recordNo));
    }

    //    비회원
    @GetMapping("/guest/records/{recordNo}")
    public ApiResponse<List<PlaceResponseDto>> getGuestPlaceOfRecord(
            @PathVariable Integer recordNo,
            @RequestParam String joinCode
    ) {
        return ApiResponse.success(placeService.getGuestPlaceOfRecord(recordNo, joinCode));
    }

    @DeleteMapping("/{placeNo}")
    public ApiResponse<Void> deletePlace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer placeNo
    ) {
        placeService.deletePlace(userDetails.getUserNo(), placeNo);
        return ApiResponse.success(null);
    }
}
