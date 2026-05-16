package com.team05.petmeeting.domain.shelter.errorCode

import com.team05.petmeeting.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class ShelterErrorCode(private val status: HttpStatus, private val code: String, private val message: String) : ErrorCode {
    SHELTER_NOT_FOUND(HttpStatus.NOT_FOUND, "SH-001", "보호소가 존재하지 않습니다."),
    ;

    override fun getStatus(): HttpStatus {
        return this.status
    }

    override fun getCode(): String {
        return this.code
    }

    override fun getMessage(): String {
        return this.message
    }
}
