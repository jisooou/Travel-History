package com.project.travel.schedule.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ScheduleOrderRequestDto {
    @NotNull
    private Integer scheduleNo;

    @NotNull
    private Integer sortOrder;
}
