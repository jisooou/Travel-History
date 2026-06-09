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

import javax.swing.text.html.Option;
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
        assertThatThrownBy(() -> recordService.getRecordDetail(1, 1))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_NOT_FOUND.getMessage());

        verify(recordRepository).findByRecordNoAndIsDeletedFalse(1);
    }

    @Test
    @DisplayName("Record 생성에 성공한다")
    void create_record_success() {
//        given
        Integer userNo = 1;
        User user = User.builder()
                .email("test@test.com")
                .userName("test")
                .password("test12345")
                .build();
        RecordRequestDto requestDto = new RecordRequestDto();
        ReflectionTestUtils.setField(requestDto, "recordName", "제주 여행");
        ReflectionTestUtils.setField(requestDto, "travelType", TravelType.DOMESTIC);

        Record savedRecord = Record.builder()
                .owner(user)
                .recordName("제주 여행")
                .travelType(TravelType.DOMESTIC)
                .build();
        ReflectionTestUtils.setField(savedRecord, "recordNo", 1);

        when(userRepository.findById(userNo))
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

        User user = User.builder()
                .email("test@test.com")
                .userName("test")
                .password("test12345")
                .build();

        RecordRequestDto recordRequestDto = new RecordRequestDto();
        ReflectionTestUtils.setField(recordRequestDto, "recordName", "제주 여행");
        ReflectionTestUtils.setField(recordRequestDto, "travelType", TravelType.DOMESTIC);

        Record savedRecord = Record.builder()
                .owner(user)
                .recordName("제주 여행")
                .travelType(TravelType.DOMESTIC)
                .build();
        ReflectionTestUtils.setField(savedRecord, "recordNo", 1);

        when(userRepository.findById(userNo))
                .thenReturn(Optional.of(user));
        when(recordRepository.save(any(Record.class)))
                .thenReturn(savedRecord);

        ArgumentCaptor<Collab> collabArgumentCaptor = ArgumentCaptor.forClass(Collab.class);

//        when
        recordService.createRecord(userNo, recordRequestDto);

//        then
        verify(collabRepository).save(collabArgumentCaptor.capture());

        Collab savedCollab = collabArgumentCaptor.getValue();

        assertThat(savedCollab.getRecord()).isEqualTo(savedRecord);
        assertThat(savedCollab.getUser()).isEqualTo(user);
        assertThat(savedCollab.getRoleCode()).isEqualTo(RoleCode.OWNER);
    }

    @Test
    @DisplayName("나의 Record 조회에 성공한다")
    void get_my_record_success() {
//        given
        Integer userNo = 1;

        User user = User.builder()
                .email("test@test.com")
                .userName("test")
                .password("test12345")
                .build();

        Record ownerRecord = Record.builder()
                .owner(user)
                .recordName("제주 여행")
                .travelType(TravelType.DOMESTIC)
                .build();
        ReflectionTestUtils.setField(ownerRecord, "recordNo", 1);

        Record editorRecord = Record.builder()
                .owner(user)
                .recordName("일본 여행")
                .travelType(TravelType.OVERSEAS)
                .build();
        ReflectionTestUtils.setField(editorRecord, "recordNo", 2);

        Collab ownerCollab = Collab.builder()
                .record(ownerRecord)
                .user(user)
                .roleCode(RoleCode.OWNER)
                .build();

        Collab editorCollab = Collab.builder()
                .record(editorRecord)
                .user(user)
                .roleCode(RoleCode.EDITOR)
                .build();

        when(collabRepository.findAllByUser_UserNoAndRoleCodeIn(
                userNo,
                List.of(RoleCode.OWNER, RoleCode.EDITOR)
        )).thenReturn(List.of(ownerCollab, editorCollab));

//        when
        List<RecordResponseDto> responseDtos = recordService.getMyRecords(userNo);

//        then
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(RecordResponseDto::getRecordName)
                .containsExactly("제주 여행", "일본 여행");
    }

    @Test
    @DisplayName("Record 상세조회에 성공한다")
    void get_record_detail_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;

        Record record = Record.builder()
                .recordName("제주 여행")
                .travelType(TravelType.DOMESTIC)
                .build();
        ReflectionTestUtils.setField(record, "recordNo", recordNo);

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.of(record));
        when(recordDayRepository.findByRecord_RecordNoOrderByTravelDateAsc(recordNo))
                .thenReturn(List.of());
        when(scheduleRepository.findByDay_Record_RecordNo(recordNo))
                .thenReturn(List.of());
        when(todoRepository.findByDay_Record_RecordNoOrderByCreatedAtAsc(recordNo))
                .thenReturn(List.of());

//        when
        RecordDetailResponseDto detailResponseDto = recordService.getRecordDetail(userNo, recordNo);

//        then
        assertThat(detailResponseDto).isNotNull();

        verify(collabAuthorityService).checkViewable(recordNo, userNo);
        verify(recordDayRepository).findByRecord_RecordNoOrderByTravelDateAsc(recordNo);
        verify(scheduleRepository).findByDay_Record_RecordNo(recordNo);
        verify(todoRepository).findByDay_Record_RecordNoOrderByCreatedAtAsc(recordNo);
    }

    @Test
    @DisplayName("Record를 수정을 할 수 있고, Editor 권한으로 저장된다")
    void update_record_success_check_editable() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;

        Record record = Record.builder()
                .recordName("제주 여행")
                .travelType(TravelType.DOMESTIC)
                .build();
        ReflectionTestUtils.setField(record, "recordNo", recordNo);

        RecordRequestDto requestDto = new RecordRequestDto();
        ReflectionTestUtils.setField(requestDto, "recordName", "일본 여행");
        ReflectionTestUtils.setField(requestDto, "travelType", TravelType.OVERSEAS);

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.of(record));

//        when
        RecordResponseDto responseDto = recordService.updateRecord(userNo, recordNo, requestDto);

//        then
        assertThat(responseDto.getRecordName()).isEqualTo("일본 여행");
        assertThat(responseDto.getTravelType()).isEqualTo(TravelType.OVERSEAS);

        verify(collabAuthorityService).checkEditable(recordNo, userNo);
    }

    @Test
    @DisplayName("Record를 삭제할 수 있고, Owner 권한으로 저장된다")
    void delete_record_success_check_owner() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;

        Record record = Record.builder()
                .recordName("제주 여행")
                .travelType(TravelType.DOMESTIC)
                .build();
        ReflectionTestUtils.setField(record, "recordNo", recordNo);

        when(recordRepository.findByRecordNoAndIsDeletedFalse(recordNo))
                .thenReturn(Optional.of(record));

//        when
        recordService.deleteRecord(userNo, recordNo);

//        then
        assertThat(record.isDeleted()).isTrue();

        verify(collabAuthorityService).checkOwner(recordNo, userNo);
    }
}
