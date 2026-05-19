package com.project.travel.record.dto.response;

import com.project.travel.record.entity.Record;
import com.project.travel.schedule.dto.response.ScheduleResponseDto;
import com.project.travel.todo.dto.response.TodoResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecordDetailResponseDto {
    //    1개의 Record 안에 여러 Days 가능
    private RecordResponseDto record;
    private List<RecordDayResponseDto> days;
    private List<ScheduleResponseDto> schedules;
    private List<TodoResponseDto> todos;

    public static RecordDetailResponseDto of(
            Record record,
            List<RecordDayResponseDto> days,
            List<ScheduleResponseDto> schedules,
            List<TodoResponseDto> todos
    ) {
        return RecordDetailResponseDto.builder()
                .record(RecordResponseDto.from(record))
                .days(days)
                .schedules(schedules)
                .todos(todos)
                .build();
    }
}
