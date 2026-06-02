package com.project.travel.collab.repository;

import com.project.travel.collab.entity.Collab;
import com.project.travel.collab.entity.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollabRepository extends JpaRepository<Collab, Integer> {
    //    어떤 사용자가 어떤 Record에 참여하는지 확인한다.
    Optional<Collab> findByRecord_RecordNoAndUser_UserNo(Integer recordNo, Integer userNo);

    //    어떤 사용자가 어떤 Record에 어떤 Role로 참여하는지 확인한다.
    List<Collab> findAllByUser_UserNoAndRoleCodeIn(Integer userNo, List<RoleCode> roleCodes);

    boolean existsByRecord_RecordNoAndUser_UserNo(Integer recordNo, Integer userNo);

    boolean existsByRecord_RecordNoAndUser_UserNoAndRoleCode(Integer recordNo, Integer userNo, RoleCode roleCode);
}
