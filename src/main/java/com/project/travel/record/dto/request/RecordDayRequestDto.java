package com.project.travel.record.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RecordDayRequestDto {
    @NotNull
    private LocalDate travelDate;

    @NotNull
    private Integer dayOrder;
}
