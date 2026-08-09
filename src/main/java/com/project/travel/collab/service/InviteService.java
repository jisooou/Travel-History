package com.project.travel.collab.service;

import com.project.travel.collab.dto.request.InviteRequestDto;
import com.project.travel.collab.dto.response.InviteResponseDto;
import com.project.travel.collab.entity.Collab;
import com.project.travel.collab.entity.InviteInfo;
import com.project.travel.collab.entity.InviteStatus;
import com.project.travel.collab.entity.RoleCode;
import com.project.travel.collab.repository.CollabRepository;
import com.project.travel.collab.repository.InviteInfoRepository;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.record.entity.Record;
import com.project.travel.record.repository.RecordRepository;
import com.project.travel.user.entity.User;
import com.project.travel.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InviteService {
    private final InviteInfoRepository inviteInfoRepository;
    private final CollabRepository collabRepository;
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final CollabAuthorityService collabAuthorityService;

    @Transactional
    public InviteResponseDto createInvite(Integer userNo, Integer recordNo, @Valid InviteRequestDto requestDto) {
//        Record: 존재하는 Record인지 확인한다.
        Record record = recordRepository.findById(recordNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
//        User: 존재하는 User인지 확인한다.
        User inviteUser = userRepository.findByEmail(requestDto.getInviteEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));
        if (collabRepository.existsByRecord_RecordNoAndUser_UserNo(recordNo, inviteUser.getUserNo())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXIST);
        }
//        Owner인지 확인한다.
        collabAuthorityService.checkMemberOwner(recordNo, userNo);

//        보류중인 InviteStatus를 확인한다.
        boolean isPending = inviteInfoRepository.existsByRecord_RecordNoAndInviteEmailAndStatus(
                recordNo,
                requestDto.getInviteEmail(),
                InviteStatus.PENDING
        );
        if (isPending) {
            throw new CustomException(ErrorCode.INVITE_STATUS_PENDING);
        }

        InviteInfo inviteInfo = InviteInfo.builder()
                .record(record)
                .inviteEmail(requestDto.getInviteEmail())
                .user(inviteUser)
                .inviteRole(requestDto.getInviteRole())
                .build();
        InviteInfo savedInviteInfo = inviteInfoRepository.save(inviteInfo);
        return InviteResponseDto.from(savedInviteInfo);
    }

    @Transactional
    public InviteResponseDto acceptInvite(Integer userNo, Integer inviteNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        InviteInfo inviteInfo = inviteInfoRepository.findByInviteNoAndStatus(inviteNo, InviteStatus.PENDING)
                .orElseThrow(() -> new CustomException(ErrorCode.INVITE_STATUS_NOT_FOUND_ACCEPT_AND_REJECT));
        if (!inviteInfo.getInviteEmail().equals(user.getEmail())) {
            throw new CustomException(ErrorCode.INVITE_STATUS_DIFFERENT_EMAIL);
        }

//        이미 참여중인지 확인한다.
        Integer recordNo = inviteInfo.getRecord().getRecordNo();
        if (collabRepository.existsByRecord_RecordNoAndUser_UserNo(recordNo, userNo)) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXIST);
        }

        Collab collab = Collab.builder()
                .record(inviteInfo.getRecord())
                .user(user)
                .roleCode(inviteInfo.getInviteRole())
                .build();
        collabRepository.save(collab);

        inviteInfo.accept();
        return InviteResponseDto.from(inviteInfo);
    }

    @Transactional
    public InviteResponseDto rejectInvite(Integer userNo, Integer inviteNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        InviteInfo inviteInfo = inviteInfoRepository.findByInviteNoAndStatus(inviteNo, InviteStatus.PENDING)
                .orElseThrow(() -> new CustomException(ErrorCode.INVITE_STATUS_NOT_FOUND_ACCEPT_AND_REJECT));
        if (!inviteInfo.getInviteEmail().equals(user.getEmail())) {
            throw new CustomException(ErrorCode.INVITE_STATUS_DIFFERENT_EMAIL);
        }

        inviteInfo.reject();
        return InviteResponseDto.from(inviteInfo);
    }
}
