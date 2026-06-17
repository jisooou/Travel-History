package com.project.travel.schedule.dto.request;

import com.project.travel.record.entity.TimeSlot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class ScheduleReorderRequestDto {
    @NotNull
    private TimeSlot timeSlot;

    @NotEmpty
    @Valid
    private List<ScheduleOrderRequestDto> schedules;
}
