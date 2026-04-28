package com.project.travel.user.controller;

import com.project.travel.global.response.ApiResponse;
import com.project.travel.user.dto.request.UserSignUpRequestDto;
import com.project.travel.user.dto.response.UserSignUpResponseDto;
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

    @PostMapping("/signup")
    public ApiResponse<UserSignUpResponseDto> signUp(@RequestBody UserSignUpRequestDto requestDto) {
        return ApiResponse.success(userService.signUp(requestDto));
    }

//    @PostMapping("/login")

}
