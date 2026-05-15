package com.project.travel.record.dto.response;

import com.project.travel.schedule.dto.response.ScheduleResponseDto;
import com.project.travel.todo.dto.response.TodoResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecordDetailResponseDto {
    private RecordResponseDto record;
    private List<RecordDayResponseDto> days;
    private List<ScheduleResponseDto> schedules;
    private List<TodoResponseDto> todos;
}
