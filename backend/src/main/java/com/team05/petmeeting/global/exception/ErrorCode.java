package com.team05.petmeeting.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getStatus();

    String getCode();  // frontend가 사용하는 식별자

    String getMessage();
}
