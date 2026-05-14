package com.team05.petmeeting.global.security.errorCode;

import com.team05.petmeeting.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SecurityErrorCode implements ErrorCode {

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "S-001", "토큰이 만료되었습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "S-002", "유효하지 않은 토큰, 로그인 재시도"),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "S-003", "접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
