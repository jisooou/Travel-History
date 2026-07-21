package com.project.travel.schedule.dto.request;

import com.project.travel.record.entity.TimeSlot;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class ScheduleRequestDto {
    @NotNull
    private Integer placeNo;

    @NotNull
    private TimeSlot timeSlot;

    @Positive
    @NotNull
    private Integer sortOrder;
}
