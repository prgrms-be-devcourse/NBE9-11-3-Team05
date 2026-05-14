package com.team05.petmeeting.domain.shelter.errorCode;

import com.team05.petmeeting.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ShelterErrorCode implements ErrorCode {

    SHELTER_NOT_FOUND(HttpStatus.NOT_FOUND, "SH-001", "보호소가 존재하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
