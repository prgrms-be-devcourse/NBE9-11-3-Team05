package com.team05.petmeeting.global.exception;

public record ValidationError(
        String field,
        String reason
) {
}