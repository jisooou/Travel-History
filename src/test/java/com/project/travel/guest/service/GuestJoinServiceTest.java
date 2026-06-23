package com.project.travel.guest.service;

import com.project.travel.collab.entity.RoleCode;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.guest.dto.request.GuestJoinRequestDto;
import com.project.travel.guest.dto.response.GuestJoinResponseDto;
import com.project.travel.guest.entity.CodeActiveStatus;
import com.project.travel.guest.entity.Guest;
import com.project.travel.guest.entity.GuestCode;
import com.project.travel.guest.repository.GuestCodeRepository;
import com.project.travel.guest.repository.GuestRepository;
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
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GuestJoinServiceTest {
    @Mock
    private GuestRepository guestRepository;
    @Mock
    private GuestCodeRepository guestCodeRepository;
    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private GuestJoinService guestJoinService;

    @Test
    @DisplayName("비회원 참여에 성공한다")
    void join_guest_success() {
//        given
        Integer recordNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo);
        GuestCode guestCode = createGuestCode(
                record,
                1,
                "ABC123",
                CodeActiveStatus.ACTIVE,
                LocalDateTime.now().plusDays(7)
        );
        GuestJoinRequestDto requestDto = createGuestJoinRequest("길동", "ABC123");
        Guest savedGuest = createGuest(record, 1, "길동", guestCode);

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(guestCodeRepository.findByJoinCodeAndIsActive("ABC123", CodeActiveStatus.ACTIVE))
                .thenReturn(Optional.of(guestCode));
        when(guestRepository.existsByRecord_RecordNoAndGuestName(recordNo, "길동"))
                .thenReturn(false);
        when(guestRepository.save(any(Guest.class)))
                .thenReturn(savedGuest);

//        when
        GuestJoinResponseDto responseDto = guestJoinService.joinGuest(recordNo, requestDto);

//        then
        assertThat(responseDto.getGuestNo()).isEqualTo(1);
        assertThat(responseDto.getGuestName()).isEqualTo("길동");
        assertThat(responseDto.getRecordNo()).isEqualTo(recordNo);
        assertThat(responseDto.getRoleCode()).isEqualTo(RoleCode.VIEWER);

        verify(guestRepository).save(any(Guest.class));
    }

    @Test
    @DisplayName("비회원 참여할 때 비회원명이 동일하면 예외가 발생한다")
    void join_guest_fail() {
//        given
        Integer recordNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo);
        GuestCode guestCode = createGuestCode(
                record,
                1,
                "ABC123",
                CodeActiveStatus.ACTIVE,
                LocalDateTime.now().plusDays(7)
        );
        GuestJoinRequestDto requestDto = createGuestJoinRequest("길동", "ABC123");

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(guestCodeRepository.findByJoinCodeAndIsActive("ABC123", CodeActiveStatus.ACTIVE))
                .thenReturn(Optional.of(guestCode));
        when(guestRepository.existsByRecord_RecordNoAndGuestName(recordNo, "길동"))
                .thenReturn(true);

//        when, then
        assertThatThrownBy(() ->
                guestJoinService.joinGuest(recordNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.GUEST_DUPLICATED_NAME.getMessage());

        verify(guestRepository, never()).save(any(Guest.class));
    }

    @Test
    @DisplayName("비회원 참여할 때 비회원 코드가 만료되었으면 예외가 발생한다")
    void join_guest_code_expired() {
//        given
        Integer recordNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo);
        GuestCode guestCode = createGuestCode(
                record,
                1,
                "ABC123",
                CodeActiveStatus.ACTIVE,
                LocalDateTime.now().minusDays(1)
        );
        GuestJoinRequestDto requestDto = createGuestJoinRequest("길동", "ABC123");

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(guestCodeRepository.findByJoinCodeAndIsActive("ABC123", CodeActiveStatus.ACTIVE))
                .thenReturn(Optional.of(guestCode));

//        when, then
        assertThatThrownBy(() ->
                guestJoinService.joinGuest(recordNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.GUEST_CODE_EXPIRED_CODE.getMessage());

        verify(guestRepository, never()).existsByRecord_RecordNoAndGuestName(anyInt(), anyString());
        verify(guestRepository, never()).save(any(Guest.class));
    }

    @Test
    @DisplayName("비회원 참여할 때 해당 Record의 비회원 코드와 일치하지 않으면 예외가 발생한다")
    void join_guest_code_active_fail() {
//        given
        Integer recordNo = 1;
        Integer otherRecordNo = 4;

        User user = createUser();
        Record record = createRecord(user, recordNo);
        Record otherRecord = createRecord(user, otherRecordNo);

        GuestCode guestCode = createGuestCode(
                otherRecord,
                1,
                "ABC123",
                CodeActiveStatus.ACTIVE,
                LocalDateTime.now().plusDays(7)
        );
        GuestJoinRequestDto requestDto = createGuestJoinRequest("길동", "ABC123");

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(guestCodeRepository.findByJoinCodeAndIsActive("ABC123", CodeActiveStatus.ACTIVE))
                .thenReturn(Optional.of(guestCode));

//        when, then
        assertThatThrownBy(() ->
                guestJoinService.joinGuest(recordNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.GUEST_CODE_INVALID_RECORD.getMessage());

        verify(guestRepository, never()).save(any(Guest.class));
    }

    @Test
    @DisplayName("비회원 참여할 때 잘못된 비회원 코드이면 예외가 발생한다")
    void join_guest_code_invalid_fail() {
//        given
        Integer recordNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo);

        GuestJoinRequestDto requestDto = createGuestJoinRequest("길동", "ABC123");

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(guestCodeRepository.findByJoinCodeAndIsActive("ABC123", CodeActiveStatus.ACTIVE))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                guestJoinService.joinGuest(recordNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.GUEST_CODE_INVALID_CODE.getMessage());

        verify(guestRepository, never()).save(any(Guest.class));
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

    private Guest createGuest(
            Record record,
            Integer guestNo,
            String guestName,
            GuestCode guestCode
    ) {
        Guest guest = Guest.builder()
                .record(record)
                .guestName(guestName)
                .guestCode(guestCode)
                .build();
        ReflectionTestUtils.setField(guest, "guestNo", guestNo);
        return guest;
    }

    private GuestJoinRequestDto createGuestJoinRequest(String guestName, String guestCode) {
        GuestJoinRequestDto requestDto = new GuestJoinRequestDto();

        ReflectionTestUtils.setField(requestDto, "guestName", guestName);
        ReflectionTestUtils.setField(requestDto, "guestCode", guestCode);
        return requestDto;
    }
}