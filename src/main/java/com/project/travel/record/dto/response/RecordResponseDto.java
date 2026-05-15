package com.project.travel.record.dto.response;

import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.TravelType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RecordResponseDto {
    private Integer recordNo;
    private UUID recordUUID;
    private String recordName;
    private TravelType travelType;
    private LocalDateTime createdAt;

    public static RecordResponseDto from(Record record) {
        return RecordResponseDto.builder()
                .recordNo(record.getRecordNo())
                .recordUUID(record.getRecordUUID())
                .recordName(record.getRecordName())
                .travelType(record.getTravelType())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
