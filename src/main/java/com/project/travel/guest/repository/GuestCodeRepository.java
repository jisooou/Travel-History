package com.project.travel.guest.repository;

import com.project.travel.guest.entity.CodeActiveStatus;
import com.project.travel.guest.entity.GuestCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuestCodeRepository extends JpaRepository<GuestCode, Integer> {
    Optional<GuestCode> findByJoinCodeAndIsActive(String joinCode, CodeActiveStatus codeActiveStatus);

    List<GuestCode> findAllByRecord_RecordNoAndIsActive(Integer recordNo, CodeActiveStatus codeActiveStatus);

    boolean existsByJoinCode(String joinCode);
}