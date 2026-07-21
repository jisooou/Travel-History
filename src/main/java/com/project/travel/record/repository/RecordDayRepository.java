package com.project.travel.record.repository;

import com.project.travel.record.entity.RecordDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecordDayRepository extends JpaRepository<RecordDay, Integer> {
    List<RecordDay> findByRecord_RecordNoAndRecord_IsDeletedFalseOrderByTravelDateAsc(Integer recordNo);
}
