package com.project.travel.user.controller;

import com.project.travel.auth.security.CustomUserDetails;
import com.project.travel.global.response.ApiResponse;
import com.project.travel.user.dto.request.EmailSendRequestDto;
import com.project.travel.user.dto.request.EmailVerifyRequestDto;
import com.project.travel.user.dto.request.UserSignUpRequestDto;
import com.project.travel.user.dto.response.UserSignUpResponseDto;
import com.project.travel.user.service.EmailService;
import com.project.travel.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/signup/email/send")
    public ApiResponse<Void> sendEmail(@Valid @RequestBody EmailSendRequestDto emailSendRequestDto) {
        emailService.sendEmailCode(emailSendRequestDto.getEmail());
        return ApiResponse.success(null);
    }

    @PostMapping("/signup/email/verify")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody EmailVerifyRequestDto emailVerifyRequestDto) {
        emailService.verifyEmail(emailVerifyRequestDto.getEmail(), emailVerifyRequestDto.getCode());
        return ApiResponse.success(null);
    }

    @PostMapping("/signup")
    public ApiResponse<UserSignUpResponseDto> signUp(@Valid @RequestBody UserSignUpRequestDto userSignUpRequestDto) {
        return ApiResponse.success(userService.signUp(userSignUpRequestDto));
    }

    @PatchMapping("/signout")
    public ApiResponse<Void> signOut(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.signOut(userDetails.getUserNo());
        return ApiResponse.success(null);
    }
}
