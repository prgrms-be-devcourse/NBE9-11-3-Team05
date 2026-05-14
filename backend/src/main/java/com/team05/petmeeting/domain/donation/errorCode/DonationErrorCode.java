package com.team05.petmeeting.domain.donation.errorCode;

import com.team05.petmeeting.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DonationErrorCode implements ErrorCode {
    DONATION_NOT_FOUND(HttpStatus.NOT_FOUND, "DO-001", "후원 내역이 없습니다."),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "DO-002", "유효하지 않은 금액입니다."),
    ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "DO-003", "이미 처리된 결제입니다."),
    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "DO-004", "결제 금액이 일치하지 않습니다."),
    PAYMENT_NOT_PAID(HttpStatus.BAD_REQUEST, "DO-005", "결제가 완료되지 않았습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}