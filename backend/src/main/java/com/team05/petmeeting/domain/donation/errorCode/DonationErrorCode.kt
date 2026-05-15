package com.team05.petmeeting.domain.donation.errorCode

import com.team05.petmeeting.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class DonationErrorCode(status: HttpStatus, code: String, message: String) : ErrorCode {
    DONATION_NOT_FOUND(HttpStatus.NOT_FOUND, "DO-001", "후원 내역이 없습니다."),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "DO-002", "유효하지 않은 금액입니다."),
    ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "DO-003", "이미 처리된 결제입니다."),
    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "DO-004", "결제 금액이 일치하지 않습니다."),
    PAYMENT_NOT_PAID(HttpStatus.BAD_REQUEST, "DO-005", "결제가 완료되지 않았습니다."),
    ;

    private val status: HttpStatus?
    private val code: String?
    private val message: String?

    init {
        this.status = status
        this.code = code
        this.message = message
    }

    override fun getStatus(): HttpStatus? {
        return this.status
    }

    override fun getCode(): String? {
        return this.code
    }

    override fun getMessage(): String? {
        return this.message
    }
}