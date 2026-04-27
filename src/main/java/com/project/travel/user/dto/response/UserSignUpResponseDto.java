package com.project.travel.user.dto;

import com.project.travel.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserResponseDto {
    private Long userNo;
    private UUID userUUID;
    private String email;
    private String userName;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .userNo(user.getUserNo())
                .userUUID(user.getUserUUID())
                .email(user.getEmail())
                .userName(user.getUserName())
                .build();
    }
}