package com.project.travel.collab.service;

import com.project.travel.collab.entity.Collab;
import com.project.travel.collab.entity.RoleCode;
import com.project.travel.collab.repository.CollabRepository;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CollabAuthorityService {
    private final CollabRepository collabRepository;

    //    RoleCode에 따른 역할 분리를 위한 중요한 Service

    //    Owner 권한 확인
    public void checkOwner(Integer recordNo, Integer userNo) {
        boolean isOwner = collabRepository.existsByRecord_RecordNoAndUser_UserNoAndRoleCode(
                recordNo,
                userNo,
                RoleCode.OWNER
        );

        if (!isOwner) {
            throw new CustomException(ErrorCode.COLLAB_AUTHORITY_OWNER);
        }
    }

    //    Editor 권한 확인
    public void checkEditable(Integer recordNo, Integer userNo) {
//        수정을 할 수 있는가? 해당 record에 초대가 되었는가?
        Collab collab = collabRepository.findByRecord_RecordNoAndUser_UserNo(recordNo, userNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_ACCESS_DENIED));

        if (!collab.canEdit()) {
            throw new CustomException(ErrorCode.COLLAB_AUTHORITY_EDITOR);
        }
    }

    //    Viewer 권한 확인
    public void checkViewable(Integer recordNo, Integer userNo) {
        boolean canView = collabRepository.existsByRecord_RecordNoAndUser_UserNo(recordNo, userNo);

        if (!canView) {
            throw new CustomException(ErrorCode.COLLAB_AUTHORITY_VIEWER);
        }
    }
}
