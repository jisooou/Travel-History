package com.project.travel.collab.entity;

import com.project.travel.record.entity.Record;
import com.project.travel.user.entity.User;
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
        name = "invite_info",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_record_email",
                        columnNames = {"RECORD_NO", "INVITE_EMAIL"}
                )
        }
)
public class InviteInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INVITE_NO")
    private Integer inviteNo;

    @Column(name = "INVITE_UUID", nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID inviteUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECORD_NO", nullable = false)
    private Record record;

    @Column(name = "INVITE_EMAIL", nullable = false)
    private String inviteEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INVITE_USER_NO")
    private User inviteUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "INVITE_ROLE", nullable = false, length = 10, comment = "EDITOR or VIEWER")
    private RoleCode inviteRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 10, comment = "NONE:상태없음 PENDING:대기 ACCEPTED:수락 REJECTED:거절")
    private InviteStatus status;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public InviteInfo(Record record, String inviteEmail, User user, RoleCode inviteRole) {
        this.record = record;
        this.inviteEmail = inviteEmail;
        this.inviteUser = user;
        this.inviteRole = inviteRole;
        this.status = InviteStatus.PENDING; //InviteService에서 InviteInfo 객체를 생성하면 InviteStatus는 PENDING이 적합하다.
    }

    @PrePersist
    public void prePersist() {
        if (this.inviteUUID == null) {
            this.inviteUUID = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public void accept() {
        this.status = InviteStatus.ACCEPTED;
    }

    public void reject() {
        this.status = InviteStatus.REJECTED;
    }
}


