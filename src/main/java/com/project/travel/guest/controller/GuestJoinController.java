package com.project.travel.guest.controller;

import com.project.travel.global.response.ApiResponse;
import com.project.travel.guest.dto.request.GuestJoinRequestDto;
import com.project.travel.guest.dto.response.GuestJoinResponseDto;
import com.project.travel.guest.service.GuestJoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guest")
public class GuestJoinController {
    private final GuestJoinService guestJoinService;

    @PostMapping("/records/{recordNo}/join")
    public ApiResponse<GuestJoinResponseDto> joinGuest(
            @PathVariable Integer recordNo,
            @Valid @RequestBody GuestJoinRequestDto requestDto
    ) {
        return ApiResponse.success(guestJoinService.joinGuest(recordNo, requestDto));
    }
}
