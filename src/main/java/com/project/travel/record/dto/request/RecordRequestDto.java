package com.project.travel.record.dto.request;

import com.project.travel.record.entity.TravelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RecordRequestDto {
    @NotBlank(message = "Record 이름을 작성해야 합니다.")
    @Size(max = 30, message = "Record 이름은 최대 30자입니다.")
    private String recordName;

    @NotNull
    private TravelType travelType;
}
