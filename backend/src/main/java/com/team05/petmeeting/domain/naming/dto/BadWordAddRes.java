package com.team05.petmeeting.domain.naming.dto;

import java.time.LocalDateTime;

// 금칙어 추가 성공 응답 DTO
public record BadWordAddRes(
        Long wordId,
        String badWord,
        LocalDateTime addedAt
) {}