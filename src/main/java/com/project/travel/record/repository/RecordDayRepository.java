package com.project.travel.record.repository;

import com.project.travel.record.entity.RecordDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordDayRepository extends JpaRepository<RecordDay, Integer> {
    List<RecordDay> findByRecord_RecordNoOrderByTravelDateAsc(Integer recordNo);
}
