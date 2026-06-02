package com.project.travel.guest.controller;

import com.project.travel.auth.security.CustomUserDetails;
import com.project.travel.global.response.ApiResponse;
import com.project.travel.guest.dto.response.GuestCodeResponseDto;
import com.project.travel.guest.service.GuestCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class GuestCodeController {
    private final GuestCodeService guestCodeService;

    //    특정 Record의 OWNER가 GuestCode를 발급해 줘야 한다.
    @PostMapping("/records/{recordNo}/join-code")
    public ApiResponse<GuestCodeResponseDto> createJoinCode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer recordNo
    ) {
        return ApiResponse.success(guestCodeService.createJoinCode(userDetails.getUserNo(), recordNo));
    }
}
