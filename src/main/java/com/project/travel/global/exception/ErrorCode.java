package com.project.travel.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //    USER
    DUPLICATE_EMAIL(400, "U001", "이미 존재하는 이메일입니다."),
    PASSWORD_MISMATCH(400, "U002", "비밀번호가 일치하지 않습니다."),
    USER_ALREADY_EXIST(400, "U003", "이미 존재하는 사용자입니다."),
    //    EMAIL
    EMAIL_CODE_EXPIRED(400, "E001", "이메일 인증 코드가 만료되었습니다."),
    EMAIL_CODE_MISMATCH(400, "E002", "이메일 인증 코드가 올바르지 않습니다."),
    NOT_VERIFIED_EMAIL(400, "E003", "잘못된 이메일입니다."),
    //    AUTH
    USER_NOT_FOUND(404, "A001", "사용자를 찾을 수 없습니다."),
    WRONG_PASSWORD(400, "A002", "잘못된 비밀번호입니다."),
    EXPIRED_REFRESH_TOKEN(400, "A003", "토큰이 만료되었거나 로그아웃된 사용자입니다."),
    DETECTED_DANGER_REFRESH_TOKEN(400, "A004", "토큰의 재사용이 감지되었습니다. 다시 로그인해 주세요."),
    LOGOUT_INVALID_TOKEN(400, "A005", "로그아웃된 토큰입니다."),
    //    RECORD
    RECORD_NOT_FOUND(404, "R001", "해당 기록을 찾을 수 없습니다."),
    RECORD_ACCESS_DENIED(400, "R002", "해당 기록에 접근할 수 없습니다."),
    RECORD_DAY_NOT_FOUND(400, "R003", "해당 기록 날짜를 찾을 수 없습니다."),
    RECORD_DAY_ACCESS_DENIED(400, "R004", "해당 기록 날짜에 접근할 수 없습니다."),
    //    PLACE
    PLACE_NOT_FOUND(404, "P001", "해당 장소를 찾을 수 없습니다."),
    //    SCHEDULE
    SCHEDULE_NOT_FOUND(404, "S001", "해당 일정을 찾을 수 없습니다."),
    SCHEDULE_ACCESS_DENIED(400, "S002", "해당 일정에 접근할 수 없습니다."),
    //    TODO
    TODO_NOT_FOUND(404, "T001", "해당 투두를 찾을 수 없습니다."),
    TODO_ACCESS_DENIED(400, "T002", "해당 투두에 접근할 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
