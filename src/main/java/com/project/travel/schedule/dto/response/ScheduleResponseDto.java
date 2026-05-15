package com.project.travel.schedule.dto.response;

import com.project.travel.record.entity.TimeSlot;
import com.project.travel.schedule.entity.SchedulePlace;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleResponseDto {
    private Integer scheduleNo;
    private Integer dayNo;
    private Integer placeNo;
    private String placeName;
    private TimeSlot timeSlot;
    private Integer sortOrder;

    public static ScheduleResponseDto from(SchedulePlace schedulePlace) {
        return ScheduleResponseDto.builder()
                .scheduleNo(schedulePlace.getScheduleNo())
                .dayNo(schedulePlace.getDay().getDayNo())
                .placeNo(schedulePlace.getPlace().getPlaceNo())
                .placeName(schedulePlace.getPlace().getPlaceName())
                .timeSlot(schedulePlace.getTimeSlot())
                .sortOrder(schedulePlace.getSortOrder())
                .build();
    }
}
