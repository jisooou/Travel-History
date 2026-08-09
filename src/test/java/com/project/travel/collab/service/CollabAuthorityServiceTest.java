package com.project.travel.collab.service;

import com.project.travel.collab.entity.Collab;
import com.project.travel.collab.entity.RoleCode;
import com.project.travel.collab.repository.CollabRepository;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.guest.entity.CodeActiveStatus;
import com.project.travel.guest.repository.GuestCodeRepository;
import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.TravelType;
import com.project.travel.record.repository.RecordRepository;
import com.project.travel.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CollabAuthorityServiceTest {
    @Mock
    private CollabRepository collabRepository;
    @Mock
    private RecordRepository recordRepository;
    @Mock
    private GuestCodeRepository guestCodeRepository;

    @InjectMocks
    private CollabAuthorityService collabAuthorityService;

    @Test
    @DisplayName("Owner 권한 확인에 성공한다")
    void check_member_owner_success() {
//        given
        Integer recordNo = 1;
        Integer userNo = 1;

        when(recordRepository.existsByRecordNoAndOwner_UserNoAndIsDeletedFalse(
                recordNo,
                userNo
        )).thenReturn(true);

//        when, then
        assertThatCode(() ->
                collabAuthorityService.checkMemberOwner(recordNo, userNo))
                .doesNotThrowAnyException();

        verify(recordRepository)
                .existsByRecordNoAndOwner_UserNoAndIsDeletedFalse(
                        recordNo,
                        userNo
                );
    }

    @Test
    @DisplayName("Owner 권한 확인에 실패하면 예외가 발생한다")
    void check_authority_owner_fail() {
//        given
        Integer recordNo = 1;
        Integer userNo = 1;

        when(recordRepository.existsByRecordNoAndOwner_UserNoAndIsDeletedFalse(
                recordNo,
                userNo
        )).thenReturn(false);

//        when, then
        assertThatThrownBy(() ->
                collabAuthorityService.checkMemberOwner(recordNo, userNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.COLLAB_AUTHORITY_OWNER.getMessage());
    }

    @Test
    @DisplayName("Editor 권한 확인에 성공한다")
    void check_member_editor_success() {
//        given
        Integer recordNo = 1;
        Integer userNo = 1;

        when(recordRepository.existsByRecordNoAndOwner_UserNoAndIsDeletedFalse(recordNo, userNo))
                .thenReturn(false);
        when(collabRepository.existsByRecord_RecordNoAndUser_UserNoAndRoleCode(recordNo, userNo, RoleCode.EDITOR))
                .thenReturn(true);

//        when, then
        assertThatCode(() ->
                collabAuthorityService.checkMemberEditor(recordNo, userNo))
                .doesNotThrowAnyException();

        verify(recordRepository)
                .existsByRecordNoAndOwner_UserNoAndIsDeletedFalse(recordNo, userNo);
        verify(collabRepository)
                .existsByRecord_RecordNoAndUser_UserNoAndRoleCode(recordNo, userNo, RoleCode.EDITOR);
    }

    @Test
    @DisplayName("Editor 권한 확인에 실패하면 예외가 발생한다")
    void check_authority_editor_fail() {
//        given
        Integer recordNo = 1;
        Integer userNo = 1;

        when(recordRepository.existsByRecordNoAndOwner_UserNoAndIsDeletedFalse(recordNo, userNo))
                .thenReturn(false);
        when(collabRepository.existsByRecord_RecordNoAndUser_UserNoAndRoleCode(
                recordNo, userNo, RoleCode.EDITOR))
                .thenReturn(false);

//        when, then
        assertThatThrownBy(() ->
                collabAuthorityService.checkMemberEditor(recordNo, userNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.COLLAB_AUTHORITY_EDITOR.getMessage());
    }

    @Test
    @DisplayName("Viewer 권한 확인에 성공한다")
    void check_authority_viewer_success() {
//        given
        Integer recordNo = 1;
        String joinCode = "ABCD1234";

        when(guestCodeRepository.existsByRecord_RecordNoAndJoinCodeAndIsActive(recordNo, joinCode, CodeActiveStatus.ACTIVE))
                .thenReturn(true);

//        when, then
        assertThatCode(() ->
                collabAuthorityService.checkGuest(recordNo, joinCode))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Viewer 권한 확인에 실패하면 예외가 발생한다")
    void check_authority_viewer_fail() {
//        given
        Integer recordNo = 1;
        String joinCode = "INVALID";

        when(guestCodeRepository.existsByRecord_RecordNoAndJoinCodeAndIsActive(recordNo, joinCode, CodeActiveStatus.ACTIVE))
                .thenReturn(false);

//        when, then
        assertThatThrownBy(() ->
                collabAuthorityService.checkGuest(recordNo, joinCode))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.COLLAB_AUTHORITY_VIEWER.getMessage());
    }

    private User createUser(Integer userNo) {
        return User.builder()
                .email("test@test.com")
                .userName("test")
                .password("test12345")
                .build();
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

    private Collab createCollab(Integer recordNo, Integer userNo, RoleCode roleCode) {
        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);

        return Collab.builder()
                .record(record)
                .user(user)
                .roleCode(roleCode)
                .build();
    }
}
