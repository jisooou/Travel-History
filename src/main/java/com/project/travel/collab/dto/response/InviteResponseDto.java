package com.project.travel.collab.dto.response;

import com.project.travel.collab.entity.InviteInfo;
import com.project.travel.collab.entity.InviteStatus;
import com.project.travel.collab.entity.RoleCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InviteResponseDto {
    private Integer inviteNo;
    private Integer recordNo;
    private String inviteEmail;
    private String inviteUserName;
    private RoleCode roleCode;
    private InviteStatus inviteStatus;

    public static InviteResponseDto from(InviteInfo inviteInfo) {
        return InviteResponseDto.builder()
                .inviteNo(inviteInfo.getInviteNo())
                .recordNo(inviteInfo.getRecord().getRecordNo())
                .inviteEmail(inviteInfo.getInviteUser().getEmail())
                .inviteUserName(inviteInfo.getInviteUser().getUserName())
                .roleCode(inviteInfo.getInviteRole())
                .inviteStatus(inviteInfo.getStatus())
                .build();
    }
}
