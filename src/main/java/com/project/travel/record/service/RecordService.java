package com.project.travel.record.service;

import com.project.travel.record.dto.request.RecordRequestDto;
import com.project.travel.record.dto.response.RecordDetailResponseDto;
import com.project.travel.record.dto.response.RecordResponseDto;
import jakarta.validation.Valid;

import java.util.List;

public class RecordService {
    public RecordResponseDto createRecord(Integer userNo, @Valid RecordRequestDto requestDto) {
    }

    public List<RecordResponseDto> getMyRecords(Integer userNo) {
        return null;
    }

    public RecordDetailResponseDto getRecordDetail(Integer userNo, Integer recordNo) {
        return null;
    }

    public RecordResponseDto updateRecord(Integer userNo, Integer recordNo, @Valid RecordRequestDto requestDto) {
        return null;
    }

    public void deleteRecord(Integer userNo, Integer recordNo) {

    }
}
