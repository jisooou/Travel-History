package com.project.travel.record.dto.response;

import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.RecordDay;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class RecordDayResponseDto {
    private Integer dayNo;
    private Integer recordNo;
    private String recordName;
    private LocalDate travelDate;
    private Integer dayOrder;

    public static RecordDayResponseDto from(RecordDay recordDay) {
        return RecordDayResponseDto.builder()
                .dayNo(recordDay.getDayNo())
                .recordNo(recordDay.getRecord().getRecordNo())
                .recordName(recordDay.getRecord().getRecordName())
                .travelDate(recordDay.getTravelDate())
                .dayOrder(recordDay.getDayOrder())
                .build();
    }
}
