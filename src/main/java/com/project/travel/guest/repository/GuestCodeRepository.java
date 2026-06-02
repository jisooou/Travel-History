package com.project.travel.guest.repository;

import com.project.travel.guest.entity.GuestCode;
import com.project.travel.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuestCodeRepository extends JpaRepository<GuestCode, Integer> {
    Optional<GuestCode> findByJoinCodeAndIsActive(String joinCode, User.ActiveStatus activeStatus);

    List<GuestCode> findAllByRecord_RecordNoAndIsActive(Integer recordNo, User.ActiveStatus activeStatus);

    boolean existsByJoinCode(String joinCode);
}