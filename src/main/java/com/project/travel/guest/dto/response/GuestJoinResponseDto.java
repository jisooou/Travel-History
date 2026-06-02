package com.project.travel.guest.dto.response;

import com.project.travel.collab.entity.RoleCode;
import com.project.travel.guest.entity.Guest;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuestJoinResponseDto {
    private Integer guestNo;
    private Integer recordNo;
    private String guestName;
    private RoleCode roleCode;

    public static GuestJoinResponseDto from(Guest guest) {
        return GuestJoinResponseDto.builder()
                .guestNo(guest.getGuestNo())
                .recordNo(guest.getRecord().getRecordNo())
                .guestName(guest.getGuestName())
                .roleCode(RoleCode.VIEWER)
                .build();
    }
}
