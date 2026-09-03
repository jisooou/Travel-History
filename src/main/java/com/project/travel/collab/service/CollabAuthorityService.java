package com.project.travel.collab.service;

import com.project.travel.collab.entity.RoleCode;
import com.project.travel.collab.repository.CollabRepository;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.guest.entity.CodeActiveStatus;
import com.project.travel.guest.repository.GuestCodeRepository;
import com.project.travel.record.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CollabAuthorityService {
    private final CollabRepository collabRepository;
    private final RecordRepository recordRepository;
    private final GuestCodeRepository guestCodeRepository;

    public void checkMemberOwner(Integer recordNo, Integer userNo) {
        boolean isOwner = recordRepository.existsByRecordNoAndOwner_UserNoAndIsDeletedFalse(recordNo, userNo);
        if (!isOwner) {
            throw new CustomException(ErrorCode.COLLAB_AUTHORITY_OWNER);
        }
    }

    public void checkMemberEditor(Integer recordNo, Integer userNo) {
        boolean isOwner = recordRepository.existsByRecordNoAndOwner_UserNoAndIsDeletedFalse(recordNo, userNo);
        if (isOwner) {
            return;
        }

        boolean isEditor = collabRepository.existsByRecord_RecordNoAndUser_UserNoAndRoleCode(
                recordNo, userNo, RoleCode.EDITOR
        );
        if (!isEditor) {
            throw new CustomException(ErrorCode.COLLAB_AUTHORITY_EDITOR);
        }
    }

    public void checkMemberViewer(Integer recordNo, Integer userNo) {
        boolean isOwner = recordRepository.existsByRecordNoAndOwner_UserNoAndIsDeletedFalse(recordNo, userNo);
        if (isOwner) {
            return;
        }
        boolean isEditor = collabRepository.existsByRecord_RecordNoAndUser_UserNoAndRoleCode(
                recordNo, userNo, RoleCode.EDITOR
        );
        if (isEditor) {
            return;
        }

        boolean isViewer = collabRepository.existsByRecord_RecordNoAndUser_UserNoAndRoleCode(
                recordNo, userNo, RoleCode.VIEWER
        );
        if (!isViewer) {
            throw new CustomException(ErrorCode.COLLAB_AUTHORITY_VIEWER);
        }
    }

    public void checkGuest(Integer recordNo, String joinCode) {
        boolean isViewer = guestCodeRepository.existsByRecord_RecordNoAndJoinCodeAndIsActive(
                recordNo, joinCode, CodeActiveStatus.ACTIVE
        );

        if (!isViewer) {
            throw new CustomException(ErrorCode.COLLAB_AUTHORITY_VIEWER);
        }
    }
}
