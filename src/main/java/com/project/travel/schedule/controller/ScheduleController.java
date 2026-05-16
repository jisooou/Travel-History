package com.project.travel.schedule.controller;

import com.project.travel.auth.security.CustomUserDetails;
import com.project.travel.global.response.ApiResponse;
import com.project.travel.schedule.dto.request.ScheduleRequestDto;
import com.project.travel.schedule.dto.response.ScheduleResponseDto;
import com.project.travel.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    //    특정 날짜에 일정 생성
    @PostMapping("/days/{dayNo}")
    public ApiResponse<ScheduleResponseDto> createSchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer dayNo,
            @Valid @RequestBody ScheduleRequestDto requestDto
    ) {
        return ApiResponse.success(scheduleService.createSchedule(userDetails.getUserNo(), dayNo, requestDto));
    }

    @GetMapping("/days/{dayNo}")
    public ApiResponse<List<ScheduleResponseDto>> getScheduleOfDay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer dayNo
    ) {
        return ApiResponse.success(scheduleService.getScheduleOfDay(userDetails.getUserNo(), dayNo));
    }

    @PatchMapping("/{scheduleNo}")
    public ApiResponse<ScheduleResponseDto> updateScheduleOfDay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer scheduleNo,
            @Valid @RequestBody ScheduleRequestDto requestDto
    ) {
        return ApiResponse.success(scheduleService.updateScheduleOfDay(userDetails.getUserNo(), scheduleNo, requestDto));
    }

    @DeleteMapping("/{scheduleNo}")
    public ApiResponse<Void> deleteScheduleOfDay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer scheduleNo
    ) {
        scheduleService.deleteScheduleOfDay(userDetails.getUserNo(), scheduleNo);
        return ApiResponse.success(null);
    }
}
