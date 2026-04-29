package com.project.travel.user.entity;

import com.project.travel.converter.UUIDConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userNo;

    @Column(name = "USER_UUID", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    @Convert(converter = UUIDConverter.class)
    private UUID userUUID;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 30, unique = true)
    private String userName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveStatus isActive;

    @Builder
    public User(String email, String userName, String password) {
        this.email = email;
        this.userName = userName;
        this.password = password;
    }

    @PrePersist
    public void prePersist() {
        if (this.userUUID == null) {
            this.userUUID = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isActive == null) {
            this.isActive = ActiveStatus.ACTIVE;
        }
    }

    public enum ActiveStatus {
        ACTIVE, INACTIVE
    }
}
