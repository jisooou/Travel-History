package com.project.travel.collab.entity;

import com.project.travel.converter.UUIDConverter;
import com.project.travel.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.project.travel.record.entity.Record;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "collab",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_collab_record_user",
                        columnNames = {"RECORD_NO", "USER_NO"}
                )
        }
)
public class Collab {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COLLAB_NO")
    private Integer collabNo;

    @Column(name = "COLLAB_UUID", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    @Convert(converter = UUIDConverter.class)
    private UUID collabUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECORD_NO", nullable = false)
    private Record record;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NO", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE_CODE", nullable = false, length = 10)
    private RoleCode roleCode;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "IS_DELETED", nullable = false)
    private boolean isDeleted = false;

    @Builder
    public Collab(Record record, User user, RoleCode roleCode) {
        this.record = record;
        this.user = user;
        this.roleCode = roleCode;
    }

    @PrePersist
    public void prePersist() {
        if (this.collabUUID == null) {
            this.collabUUID = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}