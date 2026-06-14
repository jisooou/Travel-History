package com.project.travel.record.repository;

import com.project.travel.record.entity.RecordDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecordDayRepository extends JpaRepository<RecordDay, Integer> {
    List<RecordDay> findByRecord_RecordNoOrderByTravelDateAsc(Integer recordNo);

    //    dayOrder 계산
    @Query("select max(r.dayOrder) from RecordDay r where r.record.recordNo = :recordNo")
    Optional<Integer> findMaxDayOrderByRecordNo(Integer recordNo);
}
