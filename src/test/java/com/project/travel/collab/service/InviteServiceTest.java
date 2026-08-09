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
import com.project.travel.record.entity.TravelType;
import com.project.travel.record.repository.RecordRepository;
import com.project.travel.user.entity.User;
import com.project.travel.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class InviteServiceTest {
    @Mock
    private InviteInfoRepository inviteInfoRepository;

    @Mock
    private CollabRepository collabRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CollabAuthorityService collabAuthorityService;

    @InjectMocks
    private InviteService inviteService;

    @Test
    @DisplayName("초대 생성에 성공한다")
    void create_invite_success() {
//        given
        Integer ownerNo = 1;
        Integer inviteUserNo = 2;
        Integer recordNo = 1;

        User owner = createUser(ownerNo, "owner@test.com", "owner");
        User inviteUser = createUser(inviteUserNo, "user@test.com", "user");
        Record record = createRecord(owner, recordNo);

        InviteRequestDto requestDto = createInviteRequest("user@test.com", RoleCode.EDITOR);
        InviteInfo savedInvite = createInviteInfo(
                1,
                record,
                "user@test.com",
                inviteUser,
                RoleCode.EDITOR,
                InviteStatus.PENDING
        );

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(inviteUser));
        when(collabRepository.existsByRecord_RecordNoAndUser_UserNo(recordNo, inviteUserNo))
                .thenReturn(false);
        when(inviteInfoRepository.existsByRecord_RecordNoAndInviteEmailAndStatus(recordNo, "user@test.com", InviteStatus.PENDING))
                .thenReturn(false);
        when(inviteInfoRepository.save(any(InviteInfo.class)))
                .thenReturn(savedInvite);

//        when
        InviteResponseDto responseDto = inviteService.createInvite(ownerNo, recordNo, requestDto);

//        then
        assertThat(responseDto.getInviteNo()).isEqualTo(1);
        assertThat(responseDto.getRecordNo()).isEqualTo(recordNo);
        assertThat(responseDto.getInviteEmail()).isEqualTo("user@test.com");
        assertThat(responseDto.getRoleCode()).isEqualTo(RoleCode.EDITOR);
        assertThat(responseDto.getInviteStatus()).isEqualTo(InviteStatus.PENDING);

        verify(collabAuthorityService).checkMemberOwner(recordNo, ownerNo);
        verify(inviteInfoRepository).save(any(InviteInfo.class));
    }

    @Test
    @DisplayName("존재하지 않는 Record에 초대를 생성하면 예외가 발생한다")
    void create_invite_not_found_record_fail() {
//        given
        Integer ownerNo = 1;
        Integer recordNo = 999;

        InviteRequestDto requestDto = createInviteRequest("user@test.com", RoleCode.EDITOR);

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                inviteService.createInvite(ownerNo, recordNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_NOT_FOUND.getMessage());

        verify(userRepository, never()).findByEmail(anyString());
        verify(inviteInfoRepository, never()).save(any(InviteInfo.class));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 초대하면 예외가 발생한다")
    void create_invite_not_found_email_fail() {
//        given
        Integer ownerNo = 1;
        Integer recordNo = 1;

        User owner = createUser(ownerNo, "owner@test.com", "owner");
        Record record = createRecord(owner, recordNo);

        InviteRequestDto requestDto = createInviteRequest("user@test.com", RoleCode.EDITOR);

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                inviteService.createInvite(ownerNo, recordNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.EMAIL_NOT_FOUND.getMessage());

        verify(collabAuthorityService, never()).checkMemberOwner(anyInt(), anyInt());
        verify(inviteInfoRepository, never()).save(any(InviteInfo.class));
    }

    @Test
    @DisplayName("이미 사용자가 초대되어 있으면 예외가 발생한다")
    void exist_invite_fail() {
//        given
        Integer ownerNo = 1;
        Integer inviteUserNo = 2;
        Integer recordNo = 1;

        User owner = createUser(ownerNo, "owner@test.com", "owner");
        User inviteUser = createUser(inviteUserNo, "user@test.com", "user");
        Record record = createRecord(owner, recordNo);

        InviteRequestDto requestDto = createInviteRequest("user@test.com", RoleCode.EDITOR);

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(inviteUser));
        when(collabRepository.existsByRecord_RecordNoAndUser_UserNo(recordNo, inviteUserNo))
                .thenReturn(true);

//        when, then
        assertThatThrownBy(() ->
                inviteService.createInvite(ownerNo, recordNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.USER_ALREADY_EXIST.getMessage());

        verify(collabAuthorityService, never()).checkMemberOwner(anyInt(), anyInt());
        verify(inviteInfoRepository, never()).save(any(InviteInfo.class));
    }

    @Test
    @DisplayName("이미 대기중인 초대가 있으면 예외가 발생한다")
    void create_invite_status_pending_fail() {
//        given
        Integer ownerNo = 1;
        Integer inviteUserNo = 2;
        Integer recordNo = 1;

        User owner = createUser(ownerNo, "owner@test.com", "owner");
        User inviteUser = createUser(inviteUserNo, "user@test.com", "user");
        Record record = createRecord(owner, recordNo);

        InviteRequestDto requestDto = createInviteRequest("user@test.com", RoleCode.EDITOR);

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(inviteUser));
        when(collabRepository.existsByRecord_RecordNoAndUser_UserNo(recordNo, inviteUserNo))
                .thenReturn(false);
        when(inviteInfoRepository.existsByRecord_RecordNoAndInviteEmailAndStatus(
                recordNo, "user@test.com", InviteStatus.PENDING
        )).thenReturn(true);

//        when, then
        assertThatThrownBy(() ->
                inviteService.createInvite(ownerNo, recordNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVITE_STATUS_PENDING.getMessage());

        verify(collabAuthorityService).checkMemberOwner(recordNo, ownerNo);
        verify(inviteInfoRepository, never()).save(any(InviteInfo.class));
    }

    @Test
    @DisplayName("초대 수락에 성공한다")
    void accept_invite_success() {
//        given
        Integer userNo = 2;
        Integer recordNo = 1;
        Integer inviteNo = 1;
        String email = "user@test.com";

        User owner = createUser(1, "owner@test.com", "owner");
        User inviteUser = createUser(userNo, email, "user");
        Record record = createRecord(owner, recordNo);
        InviteInfo inviteInfo = createInviteInfo(inviteNo, record, email, inviteUser, RoleCode.EDITOR, InviteStatus.PENDING);

        when(userRepository.findById(userNo))
                .thenReturn(Optional.of(inviteUser));
        when(inviteInfoRepository.findByInviteNoAndStatus(inviteNo, InviteStatus.PENDING))
                .thenReturn(Optional.of(inviteInfo));
        when(collabRepository.existsByRecord_RecordNoAndUser_UserNo(recordNo, userNo))
                .thenReturn(false);

//        when
        InviteResponseDto responseDto = inviteService.acceptInvite(userNo, inviteNo);

//        then
        assertThat(responseDto.getInviteStatus()).isEqualTo(InviteStatus.ACCEPTED);
        assertThat(responseDto.getRoleCode()).isEqualTo(RoleCode.EDITOR);

        verify(collabRepository).save(any(Collab.class));
    }

    @Test
    @DisplayName("Pending 상태인 초대가 없으면 예외가 발생한다")
    void accept_invite_status_pending_fail() {
//        given
        Integer userNo = 1;
        Integer inviteNo = 1;
        String email = "user@test.com";

        User user = createUser(userNo, email, "user");

        when(userRepository.findById(userNo))
                .thenReturn(Optional.of(user));
        when(inviteInfoRepository.findByInviteNoAndStatus(inviteNo, InviteStatus.PENDING))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                inviteService.acceptInvite(userNo, inviteNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVITE_STATUS_NOT_FOUND_ACCEPT_AND_REJECT.getMessage());

        verify(collabRepository, never()).existsByRecord_RecordNoAndUser_UserNo(anyInt(), anyInt());
        verify(collabRepository, never()).save(any(Collab.class));
    }

    @Test
    @DisplayName("초대 거절에 성공한다")
    void reject_invite_success() {
//        given
        Integer userNo = 2;
        Integer recordNo = 1;
        Integer inviteNo = 1;
        String email = "user@test.com";

        User owner = createUser(1, "owner@test.com", "owner");
        User inviteUser = createUser(userNo, email, "user");
        Record record = createRecord(owner, recordNo);
        InviteInfo inviteInfo = createInviteInfo(inviteNo, record, email, inviteUser, RoleCode.EDITOR, InviteStatus.PENDING);

        when(userRepository.findById(userNo))
                .thenReturn(Optional.of(inviteUser));
        when(inviteInfoRepository.findByInviteNoAndStatus(inviteNo, InviteStatus.PENDING))
                .thenReturn(Optional.of(inviteInfo));

//        when
        InviteResponseDto responseDto = inviteService.rejectInvite(userNo, inviteNo);

//        then
        assertThat(responseDto.getInviteStatus()).isEqualTo(InviteStatus.REJECTED);
        assertThat(responseDto.getRoleCode()).isEqualTo(RoleCode.EDITOR);

        verify(collabRepository, never()).save(any(Collab.class));
    }

    @Test
    @DisplayName("Pending 상태인 초대가 없으면 예외가 발생한다")
    void reject_invite_status_pending_fail() {
//        given
        Integer userNo = 1;
        Integer inviteNo = 1;
        String email = "user@test.com";

        User user = createUser(userNo, email, "user");

        when(userRepository.findById(userNo))
                .thenReturn(Optional.of(user));
        when(inviteInfoRepository.findByInviteNoAndStatus(inviteNo, InviteStatus.PENDING))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                inviteService.acceptInvite(userNo, inviteNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVITE_STATUS_NOT_FOUND_ACCEPT_AND_REJECT.getMessage());

        verify(collabRepository, never()).save(any(Collab.class));
    }

    private User createUser(Integer userNo, String email, String userName) {
        User user = User.builder()
                .email(email)
                .userName(userName)
                .password("test12345")
                .build();
        ReflectionTestUtils.setField(user, "userNo", userNo);
        return user;
    }

    private Record createRecord(User user, Integer recordNo) {
        Record record = Record.builder()
                .owner(user)
                .recordName("제주 여행")
                .travelType(TravelType.DOMESTIC)
                .build();
        ReflectionTestUtils.setField(record, "recordNo", recordNo);
        return record;
    }

    private InviteRequestDto createInviteRequest(String inviteEmail, RoleCode inviteRole) {
        InviteRequestDto requestDto = new InviteRequestDto();

        ReflectionTestUtils.setField(requestDto, "inviteEmail", inviteEmail);
        ReflectionTestUtils.setField(requestDto, "inviteRole", inviteRole);
        return requestDto;
    }

    private InviteInfo createInviteInfo(
            Integer inviteNo,
            Record record,
            String inviteEmail,
            User inviteUser,
            RoleCode inviteRole,
            InviteStatus status
    ) {
        InviteInfo inviteInfo = InviteInfo.builder()
                .record(record)
                .inviteEmail(inviteEmail)
                .user(inviteUser)
                .inviteRole(inviteRole)
                .build();
        ReflectionTestUtils.setField(inviteInfo, "inviteNo", inviteNo);
        ReflectionTestUtils.setField(inviteInfo, "status", status);
        return inviteInfo;
    }

}
