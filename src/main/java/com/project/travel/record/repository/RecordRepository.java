package com.project.travel.record.repository;

import com.project.travel.record.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecordRepository extends JpaRepository<Record, Integer> {
    List<Record> findByOwner_UserNoAndIsDeletedFalse(Integer userNo);

    Optional<Record> findByRecordNoAndIsDeletedFalse(Integer recordNo);
}
