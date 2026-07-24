package com.project.travel.record.entity;

import com.project.travel.converter.UUIDConverter;
import com.project.travel.record.dto.request.RecordRequestDto;
import com.project.travel.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Record {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RECORD_NO")
    private Integer recordNo;

    @Column(name = "RECORD_UUID", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    @Convert(converter = UUIDConverter.class)
    private UUID recordUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "OWNER_NO",
            referencedColumnName = "USER_NO",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_record_owner")
    )
    private User owner;

    @Column(name = "RECORD_NAME", length = 30, nullable = false)
    private String recordName;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRAVEL_TYPE", length = 20, nullable = false, comment = "DOMESTIC or OVERSEAS")
    private TravelType travelType;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "IS_DELETED", nullable = false)
    private boolean isDeleted = false;

    @Builder
    public Record(User owner, String recordName, TravelType travelType) {
        this.owner = owner;
        this.recordName = recordName;
        this.travelType = travelType;
    }

    @PrePersist
    public void prePersist() {
        if (this.recordUUID == null) {
            this.recordUUID = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    //    책임1: Record update
    public void update(RecordRequestDto requestDto) {
        this.recordName = requestDto.getRecordName();
        this.travelType = requestDto.getTravelType();
    }

    //    책임2: Record delete
    public void delete() {
        this.isDeleted = true;
    }

    public boolean isDeleted() {
        return this.isDeleted;
    }
}
