package com.project.travel.schedule.entity;

import com.project.travel.place.entity.Place;
import com.project.travel.record.entity.RecordDay;
import com.project.travel.record.entity.TimeSlot;
import com.project.travel.schedule.dto.request.ScheduleRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        comment = "일정 배치",
        name = "schedule_place",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_schedule_day_time_sort", columnNames = {"DAY_NO", "TIME_SLOT", "SORT_ORDER"})
        }
)
public class SchedulePlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCHEDULE_NO")
    private Integer scheduleNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "DAY_NO",
            nullable = false,
            referencedColumnName = "DAY_NO",
            foreignKey = @ForeignKey(name = "fk_schedule_day")
    )
    private RecordDay day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "PLACE_NO",
            nullable = false,
            referencedColumnName = "PLACE_NO",
            foreignKey = @ForeignKey(name = "fk_schedule_place")
    )
    private Place place;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIME_SLOT", length = 20, nullable = false, comment = "MORNING or AFTERNOON or NIGHT")
    private TimeSlot timeSlot;

    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public SchedulePlace(RecordDay day, Place place, TimeSlot timeSlot, Integer sortOrder) {
        this.day = day;
        this.place = place;
        this.timeSlot = timeSlot;
        this.sortOrder = sortOrder;
    }

    @PrePersist
    public void prePersist() {
        if (this.sortOrder == null) {
            this.sortOrder = 1;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    //    책임1 : Schedule update
    public void update(Place place, TimeSlot timeSlot) {
        this.place = place;
        this.timeSlot = timeSlot;
    }

    //    Schedule 재정렬을 위해 sortOrder만 변경
    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
