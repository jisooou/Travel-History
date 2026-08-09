package com.project.travel.record.controller;

import com.project.travel.auth.security.CustomUserDetails;
import com.project.travel.global.response.ApiResponse;
import com.project.travel.record.dto.request.RecordRequestDto;
import com.project.travel.record.dto.response.RecordDetailResponseDto;
import com.project.travel.record.dto.response.RecordResponseDto;
import com.project.travel.record.service.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/record")
public class RecordController {
    private final RecordService recordService;

    //    여행 기록 생성
    @PostMapping
    public ApiResponse<RecordResponseDto> createRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RecordRequestDto requestDto
    ) {
        return ApiResponse.success(
                recordService.createRecord(userDetails.getUserNo(), requestDto)
        );
    }

    //    My 여행 기록 목록 조회
    @GetMapping
    public ApiResponse<List<RecordResponseDto>> getMyRecords(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(recordService.getMyRecords(userDetails.getUserNo()));
    }

    //    회원용
    //    여행 기록 상세 조회 (날짜, 일정, todo)
    @GetMapping("/{recordNo}")
    public ApiResponse<RecordDetailResponseDto> getUserRecordDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer recordNo
    ) {
        return ApiResponse.success(recordService.getUserRecordDetail(userDetails.getUserNo(), recordNo));
    }

    //    비회원용
    @GetMapping("/guest/{recordNo}")
    public ApiResponse<RecordDetailResponseDto> getGuestRecordDetail(
            @PathVariable Integer recordNo,
            @RequestParam String joinCode
    ) {
        return ApiResponse.success(recordService.getGuestRecordDetail(recordNo, joinCode));
    }

    //  여행 기록(목록에 표시되는) 수정
    @PatchMapping("/{recordNo}")
    public ApiResponse<RecordResponseDto> updateRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer recordNo,
            @Valid @RequestBody RecordRequestDto requestDto
    ) {
        return ApiResponse.success(recordService.updateRecord(
                userDetails.getUserNo(),
                recordNo,
                requestDto
        ));
    }

    //    여행 기록 삭제
    @DeleteMapping("/{recordNo}")
    public ApiResponse<Void> deleteRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer recordNo
    ) {
        recordService.deleteRecord(userDetails.getUserNo(), recordNo);
        return ApiResponse.success(null);
    }

}
