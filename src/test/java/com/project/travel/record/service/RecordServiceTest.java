package com.project.travel.record.service;

import com.project.travel.collab.entity.Collab;
import com.project.travel.collab.entity.RoleCode;
import com.project.travel.collab.repository.CollabRepository;
import com.project.travel.collab.service.CollabAuthorityService;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.record.dto.request.RecordRequestDto;
import com.project.travel.record.dto.response.RecordDetailResponseDto;
import com.project.travel.record.dto.response.RecordResponseDto;
import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.TravelType;
import com.project.travel.record.repository.RecordDayRepository;
import com.project.travel.record.repository.RecordRepository;
import com.project.travel.schedule.repository.ScheduleRepository;
import com.project.travel.todo.repository.TodoRepository;
import com.project.travel.user.entity.User;
import com.project.travel.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {
    @Mock
    private RecordRepository recordRepository;
    @Mock
    private RecordDayRepository recordDayRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CollabRepository collabRepository;
    @Mock
    private CollabAuthorityService collabAuthorityService;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private RecordService recordService;

    @Test
    @DisplayName("존재하지 않는 Record는 예외가 발생한다")
    void record_not_found() {
//        given
        when(recordRepository.findByRecordNoAndIsDeletedFalse(1))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() -> recordService.getUserRecordDetail(1, 1))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_NOT_FOUND.getMessage());

        verify(recordRepository).findByRecordNoAndIsDeletedFalse(1);
    }

    @Test
    @DisplayName("Record 생성에 성공한다")
    void create_record_success() {
//        given
        Integer userNo = 1;
        User user = createUser();

        RecordRequestDto requestDto = createRecordRequest("제주 여행", TravelType.DOMESTIC);

        Record savedRecord = createRecord(user, 1, "제주 여행", TravelType.DOMESTIC);

        when(userRepository.findByUserNoAndIsActive(userNo, User.ActiveStatus.ACTIVE))
                .thenReturn(Optional.of(user));
        when(recordRepository.save(any(Record.class)))
                .thenReturn(savedRecord);

//        when
        RecordResponseDto recordResponseDto = recordService.createRecord(userNo, requestDto);

//        then
        assertThat(recordResponseDto.getRecordNo()).isEqualTo(1);
        assertThat(recordResponseDto.getRecordName()).isEqualTo("제주 여행");
        assertThat(recordResponseDto.getTravelType()).isEqualTo(TravelType.DOMESTIC);
    }

    @Test
    @DisplayName("Record를 생성하면 OWNER 권한으로 저장된다")
    void create_record_save_as_owner() {
//        given
        Integer userNo = 1;
        User user = createUser();

        RecordRequestDto recordRequestDto = createRecordRequest("제주 여행", TravelType.DOMESTIC);

        Record savedRecord = createRecord(user, 1, "제주 여행", TravelType.DOMESTIC);

        when(userRepository.findByUserNoAndIsActive(userNo, User.ActiveStatus.ACTIVE))
                .thenReturn(Optional.of(user));
        when(recordRepository.save(any(Record.class)))
                .thenReturn(savedRecord);

//        when
        RecordResponseDto responseDto = recordService.createRecord(userNo, recordRequestDto);

//        then
        assertThat(responseDto.getRecordName()).isEqualTo("제주 여행");

        verify(recordRepository).save(any(Record.class));
    }

    @Test
    @DisplayName("나의 Record 조회에 성공한다")
    void get_my_record_success() {
//        given
        Integer userNo = 1;

        User user = createUser();

        Record ownerRecord = createRecord(user, 1, "제주 여행", TravelType.DOMESTIC);
        Record editorRecord = createRecord(user, 2, "일본 여행", TravelType.OVERSEAS);

        when(recordRepository.findMyRecords(
                userNo,
                RoleCode.EDITOR
        )).thenReturn(List.of(ownerRecord, editorRecord));

//        when
        List<RecordResponseDto> responseDtos = recordService.getMyRecords(userNo);

//        then
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(RecordResponseDto::getRecordName)
                .containsExactly("제주 여행", "일본 여행");

        verify(recordRepository).findMyRecords(userNo, RoleCode.EDITOR);
    }

    @Test
    @DisplayName("회원 Record 상세조회에 성공한다")
    void get_user_record_detail_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;

        Record record = getRecord(1, "제주 여행", TravelType.DOMESTIC);

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.of(record));
        when(recordDayRepository.findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo))
                .thenReturn(List.of());
        when(scheduleRepository.findByDay_Record_RecordNoAndDay_Record_IsDeletedFalse(recordNo))
                .thenReturn(List.of());
        when(todoRepository.findByDay_Record_RecordNoAndDay_Record_IsDeletedFalseOrderByCreatedAtAsc(recordNo))
                .thenReturn(List.of());

//        when
        RecordDetailResponseDto detailResponseDto = recordService.getUserRecordDetail(userNo, recordNo);

//        then
        assertThat(detailResponseDto).isNotNull();

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(recordDayRepository).findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo);
        verify(scheduleRepository).findByDay_Record_RecordNoAndDay_Record_IsDeletedFalse(recordNo);
        verify(todoRepository).findByDay_Record_RecordNoAndDay_Record_IsDeletedFalseOrderByCreatedAtAsc(recordNo);
    }

    @Test
    @DisplayName("비회원 Record 상세조회에 성공한다")
    void get_guest_record_detail_success() {
//        given
        Integer recordNo = 1;
        String joinCode = "ABCD1234";

        Record record = getRecord(1, "제주 여행", TravelType.DOMESTIC);

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.of(record));
        when(recordDayRepository.findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo))
                .thenReturn(List.of());
        when(scheduleRepository.findByDay_Record_RecordNoAndDay_Record_IsDeletedFalse(recordNo))
                .thenReturn(List.of());
        when(todoRepository.findByDay_Record_RecordNoAndDay_Record_IsDeletedFalseOrderByCreatedAtAsc(recordNo))
                .thenReturn(List.of());

//        when
        RecordDetailResponseDto detailResponseDto = recordService.getGuestRecordDetail(recordNo, joinCode);

//        then
        assertThat(detailResponseDto).isNotNull();

        verify(collabAuthorityService).checkGuest(recordNo, joinCode);
        verify(recordDayRepository).findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo);
        verify(scheduleRepository).findByDay_Record_RecordNoAndDay_Record_IsDeletedFalse(recordNo);
        verify(todoRepository).findByDay_Record_RecordNoAndDay_Record_IsDeletedFalseOrderByCreatedAtAsc(recordNo);
    }

    @Test
    @DisplayName("Record를 수정을 할 수 있고, Editor 권한으로 저장된다")
    void update_record_success_check_editable() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;

        Record record = getRecord(1, "제주 여행", TravelType.DOMESTIC);

        RecordRequestDto requestDto = createRecordRequest("일본 여행", TravelType.OVERSEAS);

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.of(record));

//        when
        RecordResponseDto responseDto = recordService.updateRecord(userNo, recordNo, requestDto);

//        then
        assertThat(responseDto.getRecordName()).isEqualTo("일본 여행");
        assertThat(responseDto.getTravelType()).isEqualTo(TravelType.OVERSEAS);

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
    }

    @Test
    @DisplayName("Record를 삭제할 수 있고, Owner 권한으로 저장된다")
    void delete_record_success_check_owner() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;

        Record record = getRecord(1, "제주 여행", TravelType.DOMESTIC);

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.of(record));

//        when
        recordService.deleteRecord(userNo, recordNo);

//        then
        assertThat(record.isDeleted()).isTrue();

        verify(collabAuthorityService).checkMemberOwner(recordNo, userNo);
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

    private Record getRecord(Integer recordNo, String recordName, TravelType travelType) {
        Record record = Record.builder()
                .recordName(recordName)
                .travelType(travelType)
                .build();
        ReflectionTestUtils.setField(record, "recordNo", recordNo);
        return record;
    }

    private RecordRequestDto createRecordRequest(
            String recordName,
            TravelType travelType
    ) {
        RecordRequestDto requestDto = new RecordRequestDto();

        ReflectionTestUtils.setField(requestDto, "recordName", recordName);
        ReflectionTestUtils.setField(requestDto, "travelType", travelType);

        return requestDto;
    }
}