package com.project.travel.schedule.service;

import com.project.travel.collab.service.CollabAuthorityService;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.place.entity.Place;
import com.project.travel.place.repository.PlaceRepository;
import com.project.travel.record.entity.RecordDay;
import com.project.travel.record.repository.RecordDayRepository;
import com.project.travel.schedule.dto.request.ScheduleOrderRequestDto;
import com.project.travel.schedule.dto.request.ScheduleReorderRequestDto;
import com.project.travel.schedule.dto.request.ScheduleRequestDto;
import com.project.travel.schedule.dto.response.ScheduleResponseDto;
import com.project.travel.schedule.entity.SchedulePlace;
import com.project.travel.schedule.repository.ScheduleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        return scheduleRepository.findByDay_DayNoOrderByTimeSlotAscSortOrderAsc(dayNo)
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

    @Transactional
    public List<ScheduleResponseDto> reorderSchedules(Integer userNo, Integer dayNo, @Valid ScheduleReorderRequestDto requestDto) {
        RecordDay recordDay = getRecordDay(dayNo);
        Integer recordNo = recordDay.getRecord().getRecordNo();

        collabAuthorityService.checkEditable(recordNo, userNo);

        List<SchedulePlace> schedulePlaces = scheduleRepository.findByDayNoAndTimeSlotForUpdate(dayNo, requestDto.getTimeSlot());

//        scheduleNo로 Place를 빠르게 찾을 수 있도록 한다.
        Map<Integer, SchedulePlace> scheduleMap = schedulePlaces.stream()
                .collect(Collectors.toMap(
                        SchedulePlace::getScheduleNo,
                        Function.identity()
                ));

        validateReorderRequest(requestDto, scheduleMap);

        for (ScheduleOrderRequestDto orderRequestDto : requestDto.getSchedules()) {
            SchedulePlace schedulePlace = scheduleMap.get(orderRequestDto.getScheduleNo());
            schedulePlace.updateSortOrder(orderRequestDto.getSortOrder());
        }

        return schedulePlaces.stream()
                .sorted((schedule1, schedule2) -> schedule1.getSortOrder().compareTo(schedule2.getSortOrder()))
                .map(ScheduleResponseDto::from)
                .toList();
    }

    private void validateReorderRequest(ScheduleReorderRequestDto requestDto, Map<Integer, SchedulePlace> scheduleMap) {
//        올바른 ScheduleNo 확인
        for (ScheduleOrderRequestDto orderRequestDto : requestDto.getSchedules()) {
            if (!scheduleMap.containsKey(orderRequestDto.getScheduleNo())) {
                throw new CustomException(ErrorCode.SCHEDULE_INVALID_REORDER);
            }
        }
//        중복되지 않은 ScheduleNo 확인
        long distinctScheduleCnt = requestDto.getSchedules().stream()
                .map(ScheduleOrderRequestDto::getScheduleNo)
                .distinct()
                .count();
        if (distinctScheduleCnt != requestDto.getSchedules().size()) {
            throw new CustomException(ErrorCode.SCHEDULE_INVALID_REORDER);
        }

//        sort 하기 전과 동일한 schedule(개수) 확인
        if (requestDto.getSchedules().size() != scheduleMap.size()) {
            throw new CustomException(ErrorCode.SCHEDULE_INVALID_REORDER);
        }

//        중복되지 않은 sort 확인
        long distinctSortCnt = requestDto.getSchedules().stream()
                .map(ScheduleOrderRequestDto::getSortOrder)
                .distinct()
                .count();
        if (distinctSortCnt != requestDto.getSchedules().size()) {
            throw new CustomException(ErrorCode.SCHEDULE_INVALID_REORDER);
        }
    }
}