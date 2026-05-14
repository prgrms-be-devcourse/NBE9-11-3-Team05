package com.team05.petmeeting.domain.naming.dto;

import java.util.List;

// 후보 목록 반환 DTO
public record NameCandidateRes(
    Long animalId,
    String animalName,
    List<CandidateDto> candidateDtoList,
    int totalCandidates
) {
    public record CandidateDto(
        Long candidateId,
        Long animalId,      // 추가됨: 어떤 동물의 후보인지 식별
        String proposedName,
        String proposerNickname,
        int voteCount,
        boolean isVoted // 현재 로그인한 유저가 투표했는지 여부
    ){}
}
