package com.project.travel.record.service;

import com.project.travel.collab.service.CollabAuthorityService;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.record.dto.request.RecordDayRequestDto;
import com.project.travel.record.dto.response.RecordDayResponseDto;
import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.RecordDay;
import com.project.travel.record.repository.RecordDayRepository;
import com.project.travel.record.repository.RecordRepository;
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
    private final CollabAuthorityService collabAuthorityService;

    @Transactional
    public RecordDayResponseDto createRecordDay(Integer userNo, Integer recordNo, @Valid RecordDayRequestDto requestDto) {
        Record record = getAccessRecord(recordNo);
        collabAuthorityService.checkEditable(recordNo, userNo);

        RecordDay recordDay = RecordDay.builder()
                .record(record)
                .travelDate(requestDto.getTravelDate())
                .dayOrder(requestDto.getDayOrder())
                .build();
        RecordDay savedRecordDay = recordDayRepository.save(recordDay);
        return RecordDayResponseDto.from(savedRecordDay);
    }

    public List<RecordDayResponseDto> getRecordDays(Integer userNo, Integer recordNo) {
        getAccessRecordDay(recordNo);
        collabAuthorityService.checkViewable(recordNo, userNo);

        return recordDayRepository.findByRecord_RecordNoOrderByTravelDateAsc(recordNo)
                .stream()
                .map(RecordDayResponseDto::from)
                .toList();
    }

    @Transactional
    public RecordDayResponseDto updateRecordDay(Integer userNo, Integer dayNo, @Valid RecordDayRequestDto requestDto) {
        RecordDay recordDay = getAccessRecordDay(dayNo);
        Integer recordNo = recordDay.getRecord().getRecordNo();

        collabAuthorityService.checkEditable(recordNo, userNo);

        recordDay.update(requestDto);
        return RecordDayResponseDto.from(recordDay);
    }

    @Transactional
    public void deleteRecordDay(Integer userNo, Integer dayNo) {
        RecordDay recordDay = getAccessRecordDay(dayNo);
        Integer recordNo = recordDay.getRecord().getRecordNo();

        collabAuthorityService.checkEditable(recordNo, userNo);

        recordDayRepository.delete(recordDay);
    }

    private Record getAccessRecord(Integer recordNo) {
        return recordRepository.findById(recordNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
    }

    private RecordDay getAccessRecordDay(Integer dayNo) {
        return recordDayRepository.findById(dayNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_DAY_NOT_FOUND));
    }

}
