package com.project.travel.user.repository;

import com.project.travel.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUserUUID(UUID userUUID);

    boolean existsByUserName(String userName);
}
