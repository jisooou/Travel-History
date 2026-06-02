package com.project.travel.collab.controller;

import com.project.travel.auth.security.CustomUserDetails;
import com.project.travel.collab.dto.request.InviteRequestDto;
import com.project.travel.collab.dto.response.InviteResponseDto;
import com.project.travel.collab.service.InviteService;
import com.project.travel.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class InviteController {
    private final InviteService inviteService;

    @PostMapping("/records/{recordNo}/invite")
    public ApiResponse<InviteResponseDto> createInvite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer recordNo,
            @Valid @RequestBody InviteRequestDto requestDto
    ) {
        return ApiResponse.success(inviteService.createInvite(userDetails.getUserNo(), recordNo, requestDto));
    }

    @PostMapping("/invites/{inviteNo}/accept")
    public ApiResponse<InviteResponseDto> acceptInvite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer inviteNo
    ) {
        return ApiResponse.success(inviteService.acceptInvite(userDetails.getUserNo(), userDetails.getEmail(), inviteNo));
    }

    @PostMapping("/invites/{inviteNo}/reject")
    public ApiResponse<InviteResponseDto> rejectInvite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer inviteNo
    ) {
        return ApiResponse.success(inviteService.rejectInvite(userDetails.getUserNo(), userDetails.getEmail(), inviteNo));
    }
}
