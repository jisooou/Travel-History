package com.project.travel.user.dto.response;

import com.project.travel.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserSignUpResponseDto {
    private Integer userNo;
    private UUID userUUID;
    private String email;
    private String userName;

    public static UserSignUpResponseDto from(User user) {
        return UserSignUpResponseDto.builder()
                .userNo(user.getUserNo())
                .userUUID(user.getUserUUID())
                .email(user.getEmail())
                .userName(user.getUserName())
                .build();
    }
}