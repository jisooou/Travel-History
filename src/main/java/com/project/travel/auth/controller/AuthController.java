package com.project.travel.auth.controller;

import com.project.travel.auth.dto.request.AuthLoginRequestDto;
import com.project.travel.auth.dto.request.RefreshRequestDto;
import com.project.travel.auth.dto.response.AuthLoginResponseDto;
import com.project.travel.auth.security.CustomUserDetails;
import com.project.travel.auth.service.AuthService;
import com.project.travel.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthLoginResponseDto> login(@Valid @RequestBody AuthLoginRequestDto authLoginRequestDto) {
        return ApiResponse.success(authService.login(authLoginRequestDto));
    }

    //    refresh token 재발급
    @PostMapping("/reissue")
    public ApiResponse<AuthLoginResponseDto> reissue(@Valid @RequestBody RefreshRequestDto refreshRequestDto) {
        return ApiResponse.success(authService.reissue(refreshRequestDto));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        authService.logout(accessToken);
        return ApiResponse.success(null);
    }

    //    확인용
    @GetMapping("/me")
    public ApiResponse<Integer> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(userDetails.getUserNo());
    }
}
