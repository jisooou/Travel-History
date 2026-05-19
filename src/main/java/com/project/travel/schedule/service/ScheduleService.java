package com.project.travel.schedule.service;

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

    @Transactional
    public ScheduleResponseDto createSchedule(Integer userNo, Integer dayNo, @Valid ScheduleRequestDto requestDto) {
        RecordDay recordDay = getRecordDay(userNo, dayNo);
        Place place = placeRepository.findById(requestDto.getPlaceNo())
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

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
        getRecordDay(userNo, dayNo);
        return scheduleRepository.findByDay_DayNo(dayNo)
                .stream()
                .map(ScheduleResponseDto::from)
                .toList();
    }

    @Transactional
    public ScheduleResponseDto updateScheduleOfDay(Integer userNo, Integer scheduleNo, @Valid ScheduleRequestDto requestDto) {
        SchedulePlace schedulePlace = getMySchedule(userNo, scheduleNo);
        Place place = placeRepository.findById(requestDto.getPlaceNo())
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
        schedulePlace.update(requestDto, place);
        return ScheduleResponseDto.from(schedulePlace);
    }

    @Transactional
    public void deleteScheduleOfDay(Integer userNo, Integer scheduleNo) {
        SchedulePlace schedulePlace = getMySchedule(userNo, scheduleNo);
        scheduleRepository.delete(schedulePlace);
    }

    private RecordDay getRecordDay(Integer userNo, Integer recordDayNo) {
        RecordDay recordDay = recordDayRepository.findById(recordDayNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_DAY_NOT_FOUND));

        if (!recordDay.getRecord().getOwner().getUserNo().equals(userNo)) {
            throw new CustomException(ErrorCode.RECORD_DAY_ACCESS_DENIED);
        }
        return recordDay;
    }

    private SchedulePlace getMySchedule(Integer userNo, Integer scheduleNo) {
        SchedulePlace schedulePlace = scheduleRepository.findById(scheduleNo)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (!schedulePlace.getDay().getRecord().getOwner().getUserNo().equals(userNo)) {
            throw new CustomException(ErrorCode.SCHEDULE_ACCESS_DENIED);
        }
        return schedulePlace;
    }
}
