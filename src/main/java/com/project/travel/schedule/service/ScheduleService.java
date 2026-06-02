package com.project.travel.schedule.service;

import com.project.travel.collab.service.CollabAuthorityService;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.place.entity.Place;
import com.project.travel.place.repository.PlaceRepository;
import com.project.travel.record.entity.RecordDay;
import com.project.travel.record.repository.RecordDayRepository;
import com.project.travel.schedule.dto.request.ScheduleRequestDto;
import com.project.travel.schedule.dto.response.ScheduleResponseDto;
import com.project.travel.schedule.entity.SchedulePlace;
import com.project.travel.schedule.repository.ScheduleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final RecordDayRepository recordDayRepository;
    private final PlaceRepository placeRepository;
    private final CollabAuthorityService collabAuthorityService;

    @Transactional
    public ScheduleResponseDto createSchedule(Integer userNo, Integer dayNo, @Valid ScheduleRequestDto requestDto) {
        RecordDay recordDay = getRecordDay(dayNo);
        Place place = checkPlaceInSameRecord(requestDto.getPlaceNo(), recordDay);

        Integer recordNo = recordDay.getRecord().getRecordNo();
        collabAuthorityService.checkEditable(recordNo, userNo);

        SchedulePlace schedulePlace = SchedulePlace.builder()
                .day(recordDay)
                .place(place)
                .timeSlot(requestDto.getTimeSlot())
                .sortOrder(requestDto.getSortOrder())
                .build();
        SchedulePlace savedSchedulePlace = scheduleRepository.save(schedulePlace);
        return ScheduleResponseDto.from(savedSchedulePlace);
    }

    public List<ScheduleResponseDto> getScheduleOfDay(Integer userNo, Integer dayNo) {
        RecordDay recordDay = getRecordDay(dayNo);
        Integer recordNo = recordDay.getRecord().getRecordNo();

        collabAuthorityService.checkViewable(recordNo, userNo);

        return scheduleRepository.findByDay_DayNo(dayNo)
                .stream()
                .map(ScheduleResponseDto::from)
                .toList();
    }

    @Transactional
    public ScheduleResponseDto updateScheduleOfDay(Integer userNo, Integer scheduleNo, @Valid ScheduleRequestDto requestDto) {
        SchedulePlace schedulePlace = getAccessSchedule(scheduleNo);
        RecordDay recordDay = schedulePlace.getDay();
        Place place = checkPlaceInSameRecord(requestDto.getPlaceNo(), recordDay);

        Integer recordNo = schedulePlace.getDay().getRecord().getRecordNo();
        collabAuthorityService.checkEditable(recordNo, userNo);

        schedulePlace.update(requestDto, place);
        return ScheduleResponseDto.from(schedulePlace);
    }

    @Transactional
    public void deleteScheduleOfDay(Integer userNo, Integer scheduleNo) {
        SchedulePlace schedulePlace = getAccessSchedule(scheduleNo);
        Integer recordNo = schedulePlace.getDay().getRecord().getRecordNo();
        collabAuthorityService.checkEditable(recordNo, userNo);

        scheduleRepository.delete(schedulePlace);
    }

    private RecordDay getRecordDay(Integer recordDayNo) {
        return recordDayRepository.findById(recordDayNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_DAY_NOT_FOUND));
    }

    private SchedulePlace getAccessSchedule(Integer scheduleNo) {
        return scheduleRepository.findById(scheduleNo)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private Place checkPlaceInSameRecord(Integer placeNo, RecordDay recordDay) {
        Place place = placeRepository.findById(placeNo)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
        if (!place.getRecord().getRecordNo().equals(recordDay.getRecord().getRecordNo())) {
            throw new CustomException(ErrorCode.PLACE_ACCESS_DENIED);
        }
        return place;
    }
}
