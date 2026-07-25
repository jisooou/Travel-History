package com.project.travel.record.service;

import com.project.travel.collab.entity.Collab;
import com.project.travel.collab.entity.RoleCode;
import com.project.travel.collab.repository.CollabRepository;
import com.project.travel.collab.service.CollabAuthorityService;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.record.dto.request.RecordRequestDto;
import com.project.travel.record.dto.response.RecordDayResponseDto;
import com.project.travel.record.dto.response.RecordDetailResponseDto;
import com.project.travel.record.dto.response.RecordResponseDto;
import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.RecordDay;
import com.project.travel.record.repository.RecordDayRepository;
import com.project.travel.record.repository.RecordRepository;
import com.project.travel.schedule.dto.response.ScheduleResponseDto;
import com.project.travel.schedule.repository.ScheduleRepository;
import com.project.travel.todo.dto.response.TodoResponseDto;
import com.project.travel.todo.repository.TodoRepository;
import com.project.travel.user.entity.User;
import com.project.travel.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {
    private final RecordRepository recordRepository;
    private final RecordDayRepository recordDayRepository;
    private final UserRepository userRepository;
    private final CollabRepository collabRepository;
    private final CollabAuthorityService collabAuthorityService;
    private final ScheduleRepository scheduleRepository;
    private final TodoRepository todoRepository;

//    해당 Service는 협업 RoleCode에 따라 역할이 다르다는 점을 확실하게 구분해야 한다.

    @Transactional
    public RecordResponseDto createRecord(Integer userNo, @Valid RecordRequestDto requestDto) {
        User user = userRepository.findByUserNoAndIsActive(userNo, User.ActiveStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Record record = Record.builder()
                .owner(user)
                .recordName(requestDto.getRecordName())
                .travelType(requestDto.getTravelType())
                .build();
        Record savedRecord = recordRepository.save(record);

//        Record를 생성한 사람 : Owner 지정
        Collab ownerCollab = Collab.builder()
                .record(savedRecord)
                .user(user)
                .roleCode(RoleCode.OWNER)
                .build();
        collabRepository.save(ownerCollab);
        return RecordResponseDto.from(savedRecord);
    }

    //    본인이 만든 Record + Editor로 초대받은 Record (Owner + Editor)
    public List<RecordResponseDto> getMyRecords(Integer userNo) {
        return collabRepository.findAllByUser_UserNoAndRoleCodeInAndRecord_IsDeletedFalse(
                        userNo,
                        List.of(RoleCode.OWNER, RoleCode.EDITOR)
                )
                .stream()
                .map(Collab::getRecord)
                .map(RecordResponseDto::from)
                .toList();
    }

    public RecordDetailResponseDto getRecordDetail(Integer userNo, Integer recordNo) {
        Record record = getAccessRecord(recordNo);
//        OWNER, EDITOR, VIEWR 모두 상세 조회가 가능하다.
        collabAuthorityService.checkViewable(recordNo, userNo);

        List<RecordDay> recordDays = recordDayRepository.findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo);
        List<RecordDayResponseDto> days = getDayOrderResponse(recordDays);

        List<ScheduleResponseDto> schedules = scheduleRepository
                .findByDay_Record_RecordNoAndDay_Record_IsDeletedFalse(recordNo)
                .stream()
                .map(ScheduleResponseDto::from)
                .toList();

        List<TodoResponseDto> todos = todoRepository
                .findByDay_Record_RecordNoAndDay_Record_IsDeletedFalseOrderByCreatedAtAsc(recordNo)
                .stream()
                .map(TodoResponseDto::from)
                .toList();

        return RecordDetailResponseDto.of(
                record,
                days,
                schedules,
                todos
        );
    }

    @Transactional
    public RecordResponseDto updateRecord(Integer userNo, Integer recordNo, @Valid RecordRequestDto requestDto) {
        Record record = getAccessRecord(recordNo);
        collabAuthorityService.checkEditable(recordNo, userNo);

        record.update(requestDto.getRecordName(), requestDto.getTravelType());
        return RecordResponseDto.from(record);
    }

    @Transactional
    public void deleteRecord(Integer userNo, Integer recordNo) {
        Record record = getAccessRecord(recordNo);
        collabAuthorityService.checkOwner(recordNo, userNo);

        record.delete();
    }

    //    Owner만 보는 것 X. Editor도 볼 수 있다.
    private Record getAccessRecord(Integer recordNo) {
        return recordRepository.findByRecordNoAndIsDeletedFalse(recordNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
    }

    private List<RecordDayResponseDto> getDayOrderResponse(List<RecordDay> recordDays) {
        List<RecordDayResponseDto> result = new ArrayList<>();
        for (int i = 0; i < recordDays.size(); i++) {
            result.add(RecordDayResponseDto.from(recordDays.get(i), i + 1));
        }
        return result;
    }
}
