package com.project.travel.collab.dto.request;

import com.project.travel.collab.entity.RoleCode;
import jakarta.validation.constraints.Email;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class InviteRequestDto {
    @Email
    @NotBlank(message = "기존 사용자의 이메일을 입력해야 합니다.")
    private String inviteEmail;

    @NotNull
    private RoleCode inviteRole;
}
