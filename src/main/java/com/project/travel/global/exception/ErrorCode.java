package com.project.travel.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //    USER
    DUPLICATE_EMAIL(400, "U001", "이미 존재하는 이메일입니다."),
    PASSWORD_MISMATCH(400, "U002", "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(404, "U003", "사용자를 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
