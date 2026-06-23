package com.project.travel.collab.service;

import com.project.travel.collab.entity.Collab;
import com.project.travel.collab.entity.RoleCode;
import com.project.travel.collab.repository.CollabRepository;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.TravelType;
import com.project.travel.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.swing.text.html.Option;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CollabAuthorityServiceTest {
    @Mock
    private CollabRepository collabRepository;

    @InjectMocks
    private CollabAuthorityService collabAuthorityService;

    @Test
    @DisplayName("Owner 권한 확인에 성공한다")
    void check_authority_owner_success() {
//        given
        Integer recordNo = 1;
        Integer userNo = 1;

        when(collabRepository.existsByRecord_RecordNoAndUser_UserNoAndRoleCode(
                recordNo,
                userNo,
                RoleCode.OWNER
        )).thenReturn(true);

//        when, then
        assertThatCode(() ->
                collabAuthorityService.checkOwner(recordNo, userNo))
                .doesNotThrowAnyException();

        verify(collabRepository)
                .existsByRecord_RecordNoAndUser_UserNoAndRoleCode(
                        recordNo,
                        userNo,
                        RoleCode.OWNER
                );
    }

    @Test
    @DisplayName("Owner 권한 확인에 실패하면 예외가 발생한다")
    void check_authority_owner_fail() {
//        given
        Integer recordNo = 1;
        Integer userNo = 1;

        when(collabRepository.existsByRecord_RecordNoAndUser_UserNoAndRoleCode(
                recordNo,
                userNo,
                RoleCode.OWNER
        )).thenReturn(false);

//        when, then
        assertThatThrownBy(() ->
                collabAuthorityService.checkOwner(recordNo, userNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.COLLAB_AUTHORITY_OWNER.getMessage());
    }

    //    Editor 권한 확인 성공 테스트와 동일하다.
    @Test
    @DisplayName("Owner가 수정 권한을 갖는 데에 성공한다")
    void check_authority_owner_edit_success() {
//        given
        Integer recordNo = 1;
        Integer userNo = 1;

        Collab collab = createCollab(recordNo, userNo, RoleCode.OWNER);

        when(collabRepository.findByRecord_RecordNoAndUser_UserNo(recordNo, userNo))
                .thenReturn(Optional.of(collab));

//        when, then
        assertThatCode(() ->
                collabAuthorityService.checkEditable(recordNo, userNo))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Editor 권한 확인에 실패하면 예외가 발생한다")
    void check_authority_editor_fail() {
//        given
        Integer recordNo = 1;
        Integer userNo = 1;

        when(collabRepository.findByRecord_RecordNoAndUser_UserNo(recordNo, userNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                collabAuthorityService.checkEditable(recordNo, userNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("Viewer는 수정 권한이 없어 예외가 발생한다")
    void check_authority_viewer_edit_fail() {
//        given
        Integer recordNo = 1;
        Integer userNo = 1;

        Collab collab = createCollab(recordNo, userNo, RoleCode.VIEWER);

        when(collabRepository.findByRecord_RecordNoAndUser_UserNo(recordNo, userNo))
                .thenReturn(Optional.of(collab));

//        when, then
        assertThatThrownBy(() ->
                collabAuthorityService.checkEditable(recordNo, userNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.COLLAB_AUTHORITY_EDITOR.getMessage());
    }

    @Test
    @DisplayName("Viewer 권한 확인에 성공한다")
    void check_authority_viewer_success() {
//        given
        Integer recordNo = 1;
        Integer userNo = 1;

        when(collabRepository.existsByRecord_RecordNoAndUser_UserNo(recordNo, userNo))
                .thenReturn(true);

//        when, then
        assertThatCode(() ->
                collabAuthorityService.checkViewable(recordNo, userNo))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Viewer 권한 확인에 실패하면 예외가 발생한다")
    void check_authority_viewer_fail() {
//        given
        Integer recordNo = 1;
        Integer userNo = 1;

        when(collabRepository.existsByRecord_RecordNoAndUser_UserNo(recordNo, userNo))
                .thenReturn(false);

//        when, then
        assertThatThrownBy(() ->
                collabAuthorityService.checkViewable(recordNo, userNo))
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
