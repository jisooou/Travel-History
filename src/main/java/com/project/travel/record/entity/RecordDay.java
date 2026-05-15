package com.project.travel.record.entity;

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
                @UniqueConstraint(name = "uk_record_day_date", columnNames = {"RECORD_NO", "TRAVEL_DATE"}),
                @UniqueConstraint(name = "uk_record_day_order", columnNames = {"RECORD_NO", "DAY_ORDER"})
        }
)
public class RecordDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "DAY_ORDER", nullable = false, comment = "1일차, 2일차...")
    private Integer dayOrder;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public RecordDay(Record record, LocalDate travelDate, Integer dayOrder) {
        this.record = record;
        this.travelDate = travelDate;
        this.dayOrder = dayOrder;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
