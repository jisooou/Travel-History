package com.project.travel.schedule.repository;

import com.project.travel.record.entity.TimeSlot;
import com.project.travel.schedule.entity.SchedulePlace;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<SchedulePlace, Integer> {
    List<SchedulePlace> findByDay_DayNo(Integer dayNo);

    List<SchedulePlace> findByDay_Record_RecordNoAndRecord_IsDeletedFalse(Integer recordNo);

    List<SchedulePlace> findByDay_DayNoOrderByTimeSlotAscSortOrderAsc(Integer dayNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    @Query("""
            select sp
            from SchedulePlace sp
            where sp.day.dayNo = :dayNo
            and sp.timeSlot = :timeSlot
            order by sp.sortOrder asc
            """)
    List<SchedulePlace> findByDayNoAndTimeSlotOrderBySortOrderAscForUpdate(
            @Param("dayNo") Integer dayNo,
            @Param("timeSlot") TimeSlot timeSlot
    );
}
