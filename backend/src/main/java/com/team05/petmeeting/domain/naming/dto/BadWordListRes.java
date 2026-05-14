package com.team05.petmeeting.domain.naming.dto;

import java.util.List;

// 금칙어 전체 조회 응답 DTO
public record BadWordListRes(
        List<BadWordDto> badWords,
        int totalCount
) {
    public record BadWordDto(
            Long wordId,
            String word,
            String addedAt
    ) {}
}