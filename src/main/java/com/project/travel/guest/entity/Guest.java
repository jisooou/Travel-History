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
@Table(
        name = "guest_participant",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_record_guest_name",
                        columnNames = {"RECORD_NO", "GUEST_NAME"}
                ),
                @UniqueConstraint(
                        name = "uk_record_guest_no",
                        columnNames = {"RECORD_NO", "GUEST_NO"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_guest_record",
                        columnList = "RECORD_NO"
                )
        }
)
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GUEST_NO")
    private Integer guestNo;

    @Column(name = "GUEST_UUID", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID guestUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECORD_NO", nullable = false)
    private Record record;

    @Column(name = "GUEST_NAME", nullable = false, length = 20)
    private String guestName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOIN_CODE_NO", nullable = false)
    private GuestCode guestCode;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "IS_ACTIVE", nullable = false, length = 10, comment = "ACTIVE or INACTIVE")
    private CodeActiveStatus codeActiveStatus;

    @Builder
    public Guest(Record record, String guestName, GuestCode guestCode) {
        this.record = record;
        this.guestName = guestName;
        this.guestCode = guestCode;
        this.codeActiveStatus = CodeActiveStatus.ACTIVE;
    }

    @PrePersist
    public void prePersist() {
        if (this.guestUUID == null) {
            this.guestUUID = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
