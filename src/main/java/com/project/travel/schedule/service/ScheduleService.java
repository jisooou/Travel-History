package com.project.travel.schedule.service;

import com.project.travel.schedule.dto.request.ScheduleRequestDto;
import com.project.travel.schedule.dto.response.ScheduleResponseDto;
import jakarta.validation.Valid;

import java.util.List;

public class ScheduleService {
    public ScheduleResponseDto createSchedule(Integer userNo, Integer dayNo, @Valid ScheduleRequestDto requestDto) {
        return null;
    }

    public List<ScheduleResponseDto> getScheduleOfDay(Integer userNo, Integer dayNo) {
    }

    public ScheduleResponseDto updateScheduleOfDay(Integer userNo, Integer scheduleNo, @Valid ScheduleRequestDto requestDto) {
    }

    public void deleteScheduleOfDay(Integer userNo, Integer scheduleNo) {

    }
}
