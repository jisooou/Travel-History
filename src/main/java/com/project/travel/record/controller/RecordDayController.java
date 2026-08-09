package com.project.travel.record.controller;

import com.project.travel.auth.security.CustomUserDetails;
import com.project.travel.global.response.ApiResponse;
import com.project.travel.record.dto.request.RecordDayRequestDto;
import com.project.travel.record.dto.response.RecordDayResponseDto;
import com.project.travel.record.service.RecordDayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/record-days")
public class RecordDayController {
    private final RecordDayService recordDayService;

    //    특정 여행 기록에 날짜 추가
    @PostMapping("/records/{recordNo}")
    public ApiResponse<RecordDayResponseDto> createRecordDay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer recordNo,
            @Valid @RequestBody RecordDayRequestDto requestDto
    ) {
        return ApiResponse.success(recordDayService.createRecordDay(userDetails.getUserNo(), recordNo, requestDto));
    }

    //    특정 여행 기록의 날짜 조회
    //    회원
    @GetMapping("/records/{recordNo}")
    public ApiResponse<List<RecordDayResponseDto>> getUserRecordDays(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer recordNo
    ) {
        return ApiResponse.success(recordDayService.getUserRecordDays(userDetails.getUserNo(), recordNo));
    }

    //    비회원
    @GetMapping("/guest/records/{recordNo}")
    public ApiResponse<List<RecordDayResponseDto>> getGuestRecordDays(
            @PathVariable Integer recordNo,
            @RequestParam String joinCode
    ) {
        return ApiResponse.success(recordDayService.getGuestRecordDays(recordNo, joinCode));
    }

    //    특정 여행 기록의 날짜 수정 (recordNo, dayNo)
    @PatchMapping("/{dayNo}")
    public ApiResponse<RecordDayResponseDto> updateRecordDay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer dayNo,
            @Valid @RequestBody RecordDayRequestDto requestDto
    ) {
        return ApiResponse.success(recordDayService.updateRecordDay(userDetails.getUserNo(), dayNo, requestDto));
    }

    //    특정 여행 기록의 날짜 삭제
    @DeleteMapping("/{dayNo}")
    public ApiResponse<Void> deleteRecordDay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer dayNo
    ) {
        recordDayService.deleteRecordDay(userDetails.getUserNo(), dayNo);
        return ApiResponse.success(null);
    }
}
