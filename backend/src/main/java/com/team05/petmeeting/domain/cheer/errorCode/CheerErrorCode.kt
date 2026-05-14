package com.team05.petmeeting.domain.cheer.errorCode

import com.team05.petmeeting.global.exception.ErrorCode
import lombok.Getter
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus

enum class CheerErrorCode(
    private val status: HttpStatus,
    private val code: String,
    private val message: String,
) : ErrorCode {
    // HttpStatus 객체 사용 및 코드에 식별자(CH-) 부여
    DAILY_CHEER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "CH-001", "오늘의 응원 하트를 모두 사용했습니다.")
    ;

    override fun getStatus(): HttpStatus = status
    override fun getCode(): String = code
    override fun getMessage(): String = message
}
