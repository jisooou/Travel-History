package com.project.travel.schedule.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class ScheduleOrderRequestDto {
    @NotNull
    private Integer scheduleNo;

    @Positive
    @NotNull
    private Integer sortOrder;
}
