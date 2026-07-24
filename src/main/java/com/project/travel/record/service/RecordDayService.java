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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
                .build();
        RecordDay savedRecordDay;

        try {
            savedRecordDay = recordDayRepository.saveAndFlush(recordDay);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.RECORD_DAY_DUPLICATED);
        }

        List<RecordDay> recordDays = recordDayRepository.findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo);
        int dayOrder = generateDayOrderResponse(recordDays, recordDay);

        return RecordDayResponseDto.from(
                savedRecordDay,
                dayOrder
        );
    }

    public List<RecordDayResponseDto> getRecordDays(Integer userNo, Integer recordNo) {
        getAccessRecord(recordNo);
        collabAuthorityService.checkViewable(recordNo, userNo);

        List<RecordDay> recordDays = recordDayRepository.findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo);

        return getDayOrderResponse(recordDays);
    }

    @Transactional
    public RecordDayResponseDto updateRecordDay(Integer userNo, Integer dayNo, @Valid RecordDayRequestDto requestDto) {
        RecordDay recordDay = getAccessRecordDay(dayNo);
        Integer recordNo = recordDay.getRecord().getRecordNo();

        collabAuthorityService.checkEditable(recordNo, userNo);
        try {
            recordDay.update(requestDto.getTravelDate());
            recordDayRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new CustomException(ErrorCode.RECORD_DAY_CONFLICT);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.RECORD_DAY_DUPLICATED);
        }

        List<RecordDay> recordDays = recordDayRepository.findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(recordNo);
        int dayOrder = generateDayOrderResponse(recordDays, recordDay);

        return RecordDayResponseDto.from(
                recordDay,
                dayOrder
        );
    }

    @Transactional
    public void deleteRecordDay(Integer userNo, Integer dayNo) {
        RecordDay recordDay = getAccessRecordDay(dayNo);
        Integer recordNo = recordDay.getRecord().getRecordNo();

        collabAuthorityService.checkEditable(recordNo, userNo);

        recordDayRepository.delete(recordDay);
    }

    private Record getAccessRecord(Integer recordNo) {
        return recordRepository.findByRecordNoAndIsDeletedFalse(recordNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
    }

    private RecordDay getAccessRecordDay(Integer dayNo) {
        return recordDayRepository.findById(dayNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_DAY_NOT_FOUND));
    }

    private List<RecordDayResponseDto> getDayOrderResponse(List<RecordDay> recordDays) {
        List<RecordDayResponseDto> result = new ArrayList<>();
        for (int i = 0; i < recordDays.size(); i++) {
            result.add(RecordDayResponseDto.from(recordDays.get(i), i + 1));
        }
        return result;
    }

    private int generateDayOrderResponse(List<RecordDay> recordDays, RecordDay recordDay) {
        for (int i = 0; i < recordDays.size(); i++) {
            if (recordDays.get(i).getDayNo().equals(recordDay.getDayNo())) {
                return i + 1;
            }
        }
        throw new CustomException(ErrorCode.RECORD_DAY_NOT_FOUND);
    }
}
