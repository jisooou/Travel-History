package com.project.travel.record.repository;

import com.project.travel.collab.entity.RoleCode;
import com.project.travel.record.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecordRepository extends JpaRepository<Record, Integer> {
    boolean existsByRecordNoAndOwner_UserNoAndIsDeletedFalse(Integer recordNo, Integer userNo);

    Optional<Record> findByRecordNoAndIsDeletedFalse(Integer recordNo);

    @Query("""
            select distinct r 
            from Record r
            left join Collab c
            on c.record = r
            where r.isDeleted = false
            and 
            (
            r.owner.userNo = :userNo
            or (c.user.userNo = :userNo and c.roleCode = :roleCode)
            )
            """)
    List<Record> findMyRecords(
            @Param("userNo") Integer userNo,
            @Param("roleCode") RoleCode roleCode
    );
}
