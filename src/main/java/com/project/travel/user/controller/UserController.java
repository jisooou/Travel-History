package com.project.travel.user.controller;

import com.project.travel.global.response.ApiResponse;
import com.project.travel.user.dto.request.EmailSendRequestDto;
import com.project.travel.user.dto.request.EmailVerifyRequestDto;
import com.project.travel.user.dto.request.UserSignUpRequestDto;
import com.project.travel.user.dto.response.UserSignUpResponseDto;
import com.project.travel.user.service.EmailService;
import com.project.travel.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/signup/email/send")
    public ApiResponse<Void> sendEmail(@RequestBody EmailSendRequestDto requestDto) {
        emailService.sendEmailCode(requestDto.getEmail());
        return ApiResponse.success(null);
    }

    @PostMapping("/signup/email/verify")
    public ApiResponse<Void> verifyEmail(@RequestBody EmailVerifyRequestDto reqeustDto) {
        emailService.verifyEmail(reqeustDto.getEmail(), reqeustDto.getCode());
        return ApiResponse.success(null);
    }

    @PostMapping("/signup")
    public ApiResponse<UserSignUpResponseDto> signUp(@RequestBody UserSignUpRequestDto requestDto) {
        return ApiResponse.success(userService.signUp(requestDto));
    }

//    @PostMapping("/login")

}
