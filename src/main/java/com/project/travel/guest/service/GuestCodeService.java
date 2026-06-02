package com.project.travel.guest.service;

import com.project.travel.collab.service.CollabAuthorityService;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.guest.dto.response.GuestCodeResponseDto;
import com.project.travel.guest.entity.GuestCode;
import com.project.travel.guest.repository.GuestCodeRepository;
import com.project.travel.record.entity.Record;
import com.project.travel.record.repository.RecordRepository;
import com.project.travel.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestCodeService {
    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 12;
    private static final int MAX_RETRY = 5;

    private final GuestCodeRepository guestCodeRepository;
    private final CollabAuthorityService collabAuthorityService;
    private final RecordRepository recordRepository;

    //    특정 Record의 OWNER가 GuestCode를 발급해 줘야 한다.
    @Transactional
    public GuestCodeResponseDto createJoinCode(Integer userNo, Integer recordNo) {
//        OWNER가 맞는지 확인한다. : CollabAuthorityService에서 확인해 준다.
        collabAuthorityService.checkOwner(recordNo, userNo);
//        없는 recordNo는 아닌지 확인한다.
        Record record = recordRepository.findById(recordNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
//        하나의 Record 안에 다른 활성화된 코드가 없는지 확인한다.
        List<GuestCode> activeCodes = guestCodeRepository.findAllByRecord_RecordNoAndIsActive(recordNo, User.ActiveStatus.ACTIVE);
//        만약 활성화 되어있는 코드가 있다면 비활성화로 변경한다. (만료가 되지 않은 코드)
        for (GuestCode code : activeCodes) {
            code.inactive();
        }
//        고유 코드를 발급한다. (발급이 정상적으로 되지 않으면 예외를 던져준다)
//        코드의 만료시간을 정해준다.
//        저장한다.
        String makeGuestCode = generateUniqueCode();
        LocalDateTime expireAt = LocalDateTime.now().plusDays(7);

        GuestCode newGuestCode = GuestCode.builder()
                .record(record)
                .joinCode(makeGuestCode)
                .expireAt(expireAt)
                .build();
        GuestCode savedGuestCode = guestCodeRepository.save(newGuestCode);
        return GuestCodeResponseDto.from(savedGuestCode);
    }

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_RETRY; i++) {
            String code = generateCode();
            if (!guestCodeRepository.existsByJoinCode(code)) {
                return code;
            }
        }
        throw new CustomException(ErrorCode.GUEST_CODE_GENERATED_FAIL);
    }

    //    무작위 문자,숫자 코드 생성
    private String generateCode() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = secureRandom.nextInt(CODE_CHARACTERS.length());
            sb.append(CODE_CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
