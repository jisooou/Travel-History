package com.project.travel.record.service;

import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.record.dto.request.RecordRequestDto;
import com.project.travel.record.dto.response.RecordDayResponseDto;
import com.project.travel.record.dto.response.RecordDetailResponseDto;
import com.project.travel.record.dto.response.RecordResponseDto;
import com.project.travel.record.entity.Record;
import com.project.travel.record.repository.RecordDayRepository;
import com.project.travel.record.repository.RecordRepository;
import com.project.travel.user.entity.User;
import com.project.travel.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {
    private final RecordRepository recordRepository;
    private final RecordDayRepository recordDayRepository;
    private final UserRepository userRepository;

    @Transactional
    public RecordResponseDto createRecord(Integer userNo, @Valid RecordRequestDto requestDto) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Record record = Record.builder()
                .owner(user)
                .recordName(requestDto.getRecordName())
                .travelType(requestDto.getTravelType())
                .build();
        Record savedRecord = recordRepository.save(record);
        return RecordResponseDto.from(savedRecord);
    }

    public List<RecordResponseDto> getMyRecords(Integer userNo) {
        return recordRepository.findByOwner_UserNoAndIsDeletedFalse(userNo)
                .stream()
                .map(RecordResponseDto::from)
                .toList();
    }

    public RecordDetailResponseDto getRecordDetail(Integer userNo, Integer recordNo) {
        Record record = getMyRecord(userNo, recordNo);
        List<RecordDayResponseDto> days = recordDayRepository
                .findByRecord_RecordNoOrderByTravelDateAsc(recordNo)
                .stream()
                .map(RecordDayResponseDto::from)
                .toList();
        return RecordDetailResponseDto.of(
                record,
                days,
                List.of(),
                List.of()
        );
    }

    @Transactional
    public RecordResponseDto updateRecord(Integer userNo, Integer recordNo, @Valid RecordRequestDto requestDto) {
        Record record = getMyRecord(userNo, recordNo);
        record.update(requestDto);
        return RecordResponseDto.from(record);
    }

    @Transactional
    public void deleteRecord(Integer userNo, Integer recordNo) {
        Record record = getMyRecord(userNo, recordNo);
        record.delete();
    }

    private Record getMyRecord(Integer userNo, Integer recordNo) {
        Record record = recordRepository.findByRecordNoAndIsDeletedFalse(recordNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
        if (!record.getOwner().getUserNo().equals(userNo)) {
            throw new CustomException(ErrorCode.RECORD_ACCESS_DENIED);
        }
        return record;
    }
}
