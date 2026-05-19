package com.project.travel.record.service;

import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.record.dto.request.RecordDayRequestDto;
import com.project.travel.record.dto.response.RecordDayResponseDto;
import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.RecordDay;
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
public class RecordDayService {
    private final RecordDayRepository recordDayRepository;
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    @Transactional
    public RecordDayResponseDto createRecordDay(Integer userNo, Integer recordNo, @Valid RecordDayRequestDto requestDto) {
        Record record = getMyRecord(userNo, recordNo);
        RecordDay recordDay = RecordDay.builder()
                .record(record)
                .travelDate(requestDto.getTravelDate())
                .dayOrder(requestDto.getDayOrder())
                .build();
        RecordDay savedRecordDay = recordDayRepository.save(recordDay);
        return RecordDayResponseDto.from(savedRecordDay);
    }

    public List<RecordDayResponseDto> getRecordDays(Integer userNo, Integer recordNo) {
        getMyRecordDay(userNo, recordNo);
        return recordDayRepository.findByRecord_RecordNoOrderByTravelDateAsc(recordNo)
                .stream()
                .map(RecordDayResponseDto::from)
                .toList();
    }

    @Transactional
    public RecordDayResponseDto updateRecordDay(Integer userNo, Integer dayNo, @Valid RecordDayRequestDto requestDto) {
        RecordDay recordDay = getMyRecordDay(userNo, dayNo);
        recordDay.update(requestDto);
        return RecordDayResponseDto.from(recordDay);
    }

    @Transactional
    public void deleteRecordDay(Integer userNo, Integer dayNo) {
        RecordDay recordDay = getMyRecordDay(userNo, dayNo);
        recordDayRepository.delete(recordDay);
    }

    private Record getMyRecord(Integer userNo, Integer recordNo) {
        Record record = recordRepository.findById(recordNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        if (!record.getOwner().getUserNo().equals(userNo)) {
            throw new CustomException(ErrorCode.RECORD_ACCESS_DENIED);
        }
        return record;
    }

    private RecordDay getMyRecordDay(Integer userNo, Integer dayNo) {
        RecordDay recordDay = recordDayRepository.findById(dayNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_DAY_NOT_FOUND));
        if (!recordDay.getRecord().getOwner().getUserNo().equals(userNo)) {
            throw new CustomException(ErrorCode.RECORD_ACCESS_DENIED);
        }
        return recordDay;
    }

}
