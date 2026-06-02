package com.project.travel.guest.dto.response;

import com.project.travel.guest.entity.CodeActiveStatus;
import com.project.travel.guest.entity.GuestCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GuestCodeResponseDto {
    private Integer recordNo;
    private String joinCode;
    private CodeActiveStatus codeActiveStatus;
    private LocalDateTime expireAt;

    public static GuestCodeResponseDto from(GuestCode guestCode) {
        return GuestCodeResponseDto.builder()
                .recordNo(guestCode.getRecord().getRecordNo())
                .joinCode(guestCode.getJoinCode())
                .codeActiveStatus(guestCode.getIsActive())
                .expireAt(guestCode.getExpireAt())
                .build();
    }
}
