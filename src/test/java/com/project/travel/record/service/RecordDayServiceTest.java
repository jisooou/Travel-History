package com.project.travel.record.service;

import com.project.travel.collab.service.CollabAuthorityService;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.record.dto.request.RecordDayRequestDto;
import com.project.travel.record.dto.response.RecordDayResponseDto;
import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.RecordDay;
import com.project.travel.record.entity.TravelType;
import com.project.travel.record.repository.RecordDayRepository;
import com.project.travel.record.repository.RecordRepository;
import com.project.travel.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RecordDayServiceTest {
    @Mock
    private RecordDayRepository recordDayRepository;
    @Mock
    private RecordRepository recordRepository;
    @Mock
    private CollabAuthorityService collabAuthorityService;

    @InjectMocks
    private RecordDayService recordDayService;

    @Test
    @DisplayName("RecordDay 생성에 성공한다")
    void create_record_day_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo, "제주 여행", TravelType.DOMESTIC);

        RecordDayRequestDto requestDto = createRecordDayRequest(
                LocalDate.of(2026, 1, 1)
        );

        RecordDay day1 = createRecordDay(
                record,
                1,
                LocalDate.of(2026, 1, 1)
        );

        RecordDay day2 = createRecordDay(
                record,
                2,
                LocalDate.of(2026, 1, 2)
        );

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.of(record));
        when(recordDayRepository.saveAndFlush(any(RecordDay.class)))
                .thenReturn(day2);
        when(recordDayRepository.findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo))
                .thenReturn(List.of(day1, day2));

//        when
        RecordDayResponseDto responseDto = recordDayService.createRecordDay(userNo, recordNo, requestDto);

//        then
        assertThat(responseDto.getDayNo()).isEqualTo(2);
        assertThat(responseDto.getTravelDate()).isEqualTo(LocalDate.of(2026, 1, 2));
        assertThat(responseDto.getDayOrder()).isEqualTo(2);

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(recordDayRepository).saveAndFlush(any(RecordDay.class));
    }

    @Test
    @DisplayName("다음 날짜의 dayOrder 생성에 성공한다")
    void create_next_record_day_day_order_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo, "제주 여행", TravelType.DOMESTIC);

        RecordDayRequestDto requestDto = createRecordDayRequest(
                LocalDate.of(2026, 1, 2)
        );

        RecordDay comparedRecordDay = createRecordDay(
                record,
                1,
                LocalDate.of(2026, 1, 1)
        );

        RecordDay savedRecordDay = createRecordDay(
                record,
                2,
                LocalDate.of(2026, 1, 2)
        );

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.of(record));
        when(recordDayRepository.saveAndFlush(any(RecordDay.class)))
                .thenReturn(savedRecordDay);
        when(recordDayRepository.findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo))
                .thenReturn(List.of(comparedRecordDay, savedRecordDay));

//        when
        RecordDayResponseDto responseDto = recordDayService.createRecordDay(userNo, recordNo, requestDto);

//        then
        assertThat(responseDto.getDayOrder()).isEqualTo(2);
        assertThat(responseDto.getTravelDate()).isEqualTo(LocalDate.of(2026, 1, 2));

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(recordDayRepository).saveAndFlush(any(RecordDay.class));
    }

    @Test
    @DisplayName("존재하지 않는 Record에 RecordDay를 생성하면 예외가 발생한다")
    void create_record_day_fail() {
//        given
        Integer userNo = 1;
        Integer recordNo = 999;

        RecordDayRequestDto requestDto = createRecordDayRequest(
                LocalDate.of(2026, 1, 1)
        );

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                recordDayService.createRecordDay(userNo, recordNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_NOT_FOUND.getMessage());

        verify(collabAuthorityService, never()).checkMemberEditor(anyInt(), anyInt());
        verify(recordDayRepository, never()).save(any(RecordDay.class));
    }

    @Test
    @DisplayName("회원 RecordDay 조회에 성공한다")
    void get_user_record_day_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo, "제주 여행", TravelType.DOMESTIC);
        RecordDay day1 = createRecordDay(
                record,
                1,
                LocalDate.of(2026, 1, 1)
        );
        RecordDay day2 = createRecordDay(
                record,
                2,
                LocalDate.of(2026, 1, 2)
        );

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.of(record));
        when(recordDayRepository.findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo))
                .thenReturn(List.of(day1, day2));

//        when
        List<RecordDayResponseDto> responseDtos = recordDayService.getUserRecordDays(userNo, recordNo);

//        then
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(RecordDayResponseDto::getTravelDate)
                .containsExactly(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 2)
                );
        assertThat(responseDtos)
                .extracting(RecordDayResponseDto::getDayOrder)
                .containsExactly(1, 2);

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(recordDayRepository).findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo);
    }

    @Test
    @DisplayName("존재하지 않는 Record의 RecordDay(날짜)를 조회하면 예외가 발생한다")
    void get_record_day_fail() {
//        given
        Integer userNo = 1;
        Integer recordNo = 999;

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                recordDayService.getUserRecordDays(userNo, recordNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_NOT_FOUND.getMessage());

        verify(collabAuthorityService, never()).checkMemberEditor(anyInt(), anyInt());
        verify(recordDayRepository, never()).findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(anyInt());
    }

    @Test
    @DisplayName("비회원 RecordDay 조회에 성공한다")
    void get_guest_record_day_success() {
//        given
        Integer recordNo = 1;
        String joinCode = "ABCD1234";

        User user = createUser();
        Record record = createRecord(user, recordNo, "제주 여행", TravelType.DOMESTIC);
        RecordDay day1 = createRecordDay(
                record,
                1,
                LocalDate.of(2026, 1, 1)
        );
        RecordDay day2 = createRecordDay(
                record,
                2,
                LocalDate.of(2026, 1, 2)
        );

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.of(record));
        when(recordDayRepository.findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo))
                .thenReturn(List.of(day1, day2));

//        when
        List<RecordDayResponseDto> responseDtos = recordDayService.getGuestRecordDays(recordNo, joinCode);

//        then
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(RecordDayResponseDto::getTravelDate)
                .containsExactly(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 2)
                );
        assertThat(responseDtos)
                .extracting(RecordDayResponseDto::getDayOrder)
                .containsExactly(1, 2);

        verify(collabAuthorityService).checkGuest(recordNo, joinCode);
        verify(recordDayRepository).findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo);
    }

    @Test
    @DisplayName("RecordDay 수정에 성공한다")
    void update_record_day_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo, "제주 여행", TravelType.DOMESTIC);
        RecordDay day1 = createRecordDay(
                record,
                1,
                LocalDate.of(2026, 1, 1)
        );
        RecordDay day2 = createRecordDay(
                record,
                2,
                LocalDate.of(2026, 1, 2)
        );
        RecordDayRequestDto requestDto = createRecordDayRequest(
                LocalDate.of(2026, 1, 3)
        );

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.of(day1));
        when(recordDayRepository.findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo))
                .thenReturn(List.of(day2, day1));

//        when
        RecordDayResponseDto responseDto = recordDayService.updateRecordDay(userNo, dayNo, requestDto);

//        then
        assertThat(responseDto.getTravelDate()).isEqualTo(LocalDate.of(2026, 1, 3));
        assertThat(responseDto.getDayOrder()).isEqualTo(2);

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
    }

    @Test
    @DisplayName("존재하지 않는 RecordDay(날짜)를 수정하면 예외가 발생한다")
    void update_record_day_fail() {
//        given
        Integer userNo = 1;
        Integer dayNo = 999;

        RecordDayRequestDto requestDto = createRecordDayRequest(
                LocalDate.of(2026, 1, 1)
        );

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                recordDayService.updateRecordDay(userNo, dayNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_DAY_NOT_FOUND.getMessage());

        verify(collabAuthorityService, never()).checkMemberEditor(anyInt(), anyInt());
    }

    @Test
    @DisplayName("RecordDay 삭제에 성공한다")
    void delete_record_day_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo, "제주 여행", TravelType.DOMESTIC);
        RecordDay recordDay = createRecordDay(
                record,
                dayNo,
                LocalDate.of(2026, 1, 1)
        );

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.of(recordDay));

//        when
        recordDayService.deleteRecordDay(userNo, dayNo);

//        then
        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(recordDayRepository).delete(recordDay);
    }

    @Test
    @DisplayName("존재하지 않는 RecordDay(날짜)를 삭제하면 예외가 발생한다")
    void delete_record_day_fail() {
//        given
        Integer userNo = 1;
        Integer dayNo = 999;

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                recordDayService.deleteRecordDay(userNo, dayNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_DAY_NOT_FOUND.getMessage());

        verify(collabAuthorityService, never()).checkMemberEditor(anyInt(), anyInt());
        verify(recordDayRepository, never()).delete(any(RecordDay.class));
    }

    private User createUser() {
        return User.builder()
                .email("test@test.com")
                .userName("test")
                .password("test12345")
                .build();
    }

    private Record createRecord(User user, Integer recordNo, String recordName, TravelType travelType) {
        Record record = Record.builder()
                .owner(user)
                .recordName(recordName)
                .travelType(travelType)
                .build();
        ReflectionTestUtils.setField(record, "recordNo", recordNo);
        return record;
    }

    private RecordDay createRecordDay(Record record, Integer dayNo, LocalDate travelDate) {
        RecordDay recordDay = RecordDay.builder()
                .record(record)
                .travelDate(travelDate)
                .build();
        ReflectionTestUtils.setField(recordDay, "dayNo", dayNo);
        return recordDay;
    }

    private RecordDayRequestDto createRecordDayRequest(LocalDate travelDate) {
        RecordDayRequestDto requestDto = new RecordDayRequestDto();

        ReflectionTestUtils.setField(requestDto, "travelDate", travelDate);
        return requestDto;
    }
}
