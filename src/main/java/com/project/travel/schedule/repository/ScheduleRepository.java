package com.project.travel.schedule.repository;

import com.project.travel.schedule.entity.SchedulePlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<SchedulePlace, Integer> {
    List<SchedulePlace> findByDay_DayNo(Integer dayNo);

    List<SchedulePlace> findByDay_Record_RecordNo(Integer recordNo);
}
