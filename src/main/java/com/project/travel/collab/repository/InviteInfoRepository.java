package com.project.travel.collab.repository;

import com.project.travel.collab.entity.InviteInfo;
import com.project.travel.collab.entity.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InviteInfoRepository extends JpaRepository<InviteInfo, Integer> {
    boolean existsByRecord_RecordNoAndInviteEmailAndStatus(
            Integer recordNo,
            String inviteEmail,
            InviteStatus inviteStatus
    );

    List<InviteInfo> findAllByInviteEmailAndStatus(String inviteEmail, InviteStatus inviteStatus);

    Optional<InviteInfo> findByInviteNoAndStatus(Integer inviteNo, InviteStatus inviteStatus);
}
