package com.project.travel.guest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GuestJoinRequestDto {
    @NotBlank
    private String guestName;

    @NotBlank
    private String guestCode;
}
