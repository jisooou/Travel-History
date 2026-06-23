package com.project.travel.guest.service;

import com.project.travel.collab.service.CollabAuthorityService;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.guest.dto.response.GuestCodeResponseDto;
import com.project.travel.guest.entity.CodeActiveStatus;
import com.project.travel.guest.entity.GuestCode;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GuestCodeServiceTest {
    @Mock
    private GuestCodeRepository guestCodeRepository;
    @Mock
    private CollabAuthorityService collabAuthorityService;
    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private GuestCodeService guestCodeService;

    @Test
    @DisplayName("비회원 발급 코드 생성에 성공한다")
    void create_join_guest_code_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo);

        GuestCode oldCode = createGuestCode(record, 1, "OLD_CODE", CodeActiveStatus.ACTIVE);
        GuestCode newCode = createGuestCode(record, 2, "NEW_CODE", CodeActiveStatus.ACTIVE);

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(guestCodeRepository.findAllByRecord_RecordNoAndIsActive(recordNo, CodeActiveStatus.ACTIVE))
                .thenReturn(List.of(oldCode));
        when(guestCodeRepository.existsByJoinCode(anyString()))
                .thenReturn(false);
        when(guestCodeRepository.save(any(GuestCode.class)))
                .thenReturn(newCode);

//        when
        GuestCodeResponseDto responseDto = guestCodeService.createJoinCode(userNo, recordNo);

//        then
        assertThat(responseDto.getRecordNo()).isEqualTo(recordNo);
        assertThat(responseDto.getCodeActiveStatus()).isEqualTo(CodeActiveStatus.ACTIVE);
        assertThat(oldCode.getIsActive()).isEqualTo(CodeActiveStatus.INACTIVE);

        verify(collabAuthorityService).checkOwner(recordNo, userNo);
        verify(guestCodeRepository).save(any(GuestCode.class));
    }

    @Test
    @DisplayName("존재하지 않는 Record에 비회원 발급 코드를 생성하면 예외가 발생한다")
    void create_join_guest_code_fail() {
//        given
        Integer userNo = 1;
        Integer recordNo = 999;

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                guestCodeService.createJoinCode(userNo, recordNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_NOT_FOUND.getMessage());

        verify(collabAuthorityService).checkOwner(recordNo, userNo);
        verify(guestCodeRepository, never()).save(any(GuestCode.class));
    }

    private User createUser() {
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

    private GuestCode createGuestCode(
            Record record,
            Integer joinCodeNo,
            String joinCode,
            CodeActiveStatus codeActiveStatus
    ) {
        return createGuestCode(
                record,
                joinCodeNo,
                joinCode,
                codeActiveStatus,
                LocalDateTime.now().plusDays(7)
        );
    }

    private GuestCode createGuestCode(
            Record record,
            Integer joinCodeNo,
            String joinCode,
            CodeActiveStatus codeActiveStatus,
            LocalDateTime expiredAt
    ) {
        GuestCode guestCode = GuestCode.builder()
                .record(record)
                .joinCode(joinCode)
                .expireAt(expiredAt)
                .build();
        ReflectionTestUtils.setField(guestCode, "joinCodeNo", joinCodeNo);
        ReflectionTestUtils.setField(guestCode, "isActive", codeActiveStatus);
        return guestCode;
    }
}
