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
    @Column(name = "USER_NO")
    private Integer userNo;

    @Column(name = "USER_UUID", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    @Convert(converter = UUIDConverter.class)
    private UUID userUUID;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "USER_NAME", length = 30, unique = true)
    private String userName;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "IS_ACTIVE", nullable = false)
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
