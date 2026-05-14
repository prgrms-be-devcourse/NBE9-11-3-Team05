package com.team05.petmeeting.domain.naming.dto;

import jakarta.validation.constraints.NotBlank;

// 금칙어 추가 요청 DTO
public record BadWordAddReq(
        @NotBlank(message = "추가할 금칙어를 입력해주세요.")
        String badWord
) {}