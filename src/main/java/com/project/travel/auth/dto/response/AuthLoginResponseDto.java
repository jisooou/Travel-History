package com.project.travel.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthLoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long accessTokenExpire;
}
