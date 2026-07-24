package com.project.travel.record.entity;

import com.project.travel.record.dto.request.RecordDayRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        comment = "날짜",
        name = "record_day",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_record_day_date", columnNames = {"RECORD_NO", "TRAVEL_DATE"})
        }
)
public class RecordDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DAY_NO")
    private Integer dayNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "RECORD_NO",
            nullable = false,
            referencedColumnName = "RECORD_NO",
            foreignKey = @ForeignKey(name = "fk_record_day_record")
    )
    private Record record;

    @Column(name = "TRAVEL_DATE", nullable = false)
    private LocalDate travelDate;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Builder
    public RecordDay(Record record, LocalDate travelDate) {
        this.record = record;
        this.travelDate = travelDate;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    //    책임1 : RecordDay update
    public void update(LocalDate travelDate) {
        this.travelDate = travelDate;
    }
}
