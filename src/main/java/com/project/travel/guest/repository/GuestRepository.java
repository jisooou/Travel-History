package com.project.travel.guest.repository;

import com.project.travel.guest.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Integer> {
    // 어떤 Record를 어떤 Guest가 가지고 있는지 확인한다.
    boolean existsByRecord_RecordNoAndGuestName(Integer recordNo, String guestName);
}