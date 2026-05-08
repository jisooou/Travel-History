package com.project.travel.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshRequestDto {
    @NotBlank
    private String refreshToken;
}
