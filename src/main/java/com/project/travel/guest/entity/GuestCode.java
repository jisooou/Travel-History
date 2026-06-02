package com.project.travel.guest.entity;

import com.project.travel.record.entity.Record;
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
@Table(name = "record_join_code")
public class GuestCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOIN_CODE_NO")
    private Integer joinCodeNo;

    @Column(name = "JOIN_CODE_UUID", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID joinCodeUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECORD_NO", nullable = false)
    private Record record;

    @Column(name = "JOIN_CODE", nullable = false, length = 50, unique = true)
    private String joinCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "IS_ACTIVE", nullable = false, length = 10, comment = "ACTIVE or INACTIVE")
    private CodeActiveStatus isActive;

    @Column(name = "EXPIRE_AT")
    private LocalDateTime expireAt;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public GuestCode(Record record, String joinCode, LocalDateTime expireAt) {
        this.record = record;
        this.joinCode = joinCode;
        this.isActive = CodeActiveStatus.ACTIVE;
        this.expireAt = expireAt;
    }

    @PrePersist
    public void prePersist() {
        if (this.joinCodeUUID == null) {
            this.joinCodeUUID = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public void inactive() {
        this.isActive = CodeActiveStatus.INACTIVE;
    }

    public boolean isExpired() {
        return expireAt != null && expireAt.isBefore(LocalDateTime.now());
    }
}