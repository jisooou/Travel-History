package com.project.travel.guest.service;

import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.guest.dto.request.GuestJoinRequestDto;
import com.project.travel.guest.dto.response.GuestJoinResponseDto;
import com.project.travel.guest.entity.Guest;
import com.project.travel.guest.entity.GuestCode;
import com.project.travel.guest.repository.GuestCodeRepository;
import com.project.travel.guest.repository.GuestRepository;
import com.project.travel.record.entity.Record;
import com.project.travel.record.repository.RecordRepository;
import com.project.travel.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuestJoinService {
    private final GuestRepository guestRepository;
    private final GuestCodeRepository guestCodeRepository;
    private final RecordRepository recordRepository;

    @Transactional
    public GuestJoinResponseDto joinGuest(Integer recordNo, @Valid GuestJoinRequestDto requestDto) {
        Record record = recordRepository.findById(recordNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        GuestCode guestCode = guestCodeRepository
                .findByJoinCodeAndIsActive(requestDto.getGuestCode(), User.ActiveStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.GUEST_CODE_INVALID_CODE));
        if (!guestCode.getRecord().getRecordNo().equals(recordNo)) {
            throw new CustomException(ErrorCode.GUEST_CODE_INVALID_RECORD);
        }
        if (guestCode.isExpired()) {
            throw new CustomException(ErrorCode.GUEST_CODE_EXPIRED_CODE);
        }

        boolean duplicatedName = guestRepository.existsByRecord_RecordNoAndGuestName(recordNo, requestDto.getGuestName());
        if (duplicatedName) {
            throw new CustomException(ErrorCode.GUEST_DUPLICATED_NAME);
        }

        Guest guest = Guest.builder()
                .record(record)
                .guestName(requestDto.getGuestName())
                .guestCode(guestCode)
                .build();
        Guest savedGuest = guestRepository.save(guest);
        return GuestJoinResponseDto.from(savedGuest);
    }
}
