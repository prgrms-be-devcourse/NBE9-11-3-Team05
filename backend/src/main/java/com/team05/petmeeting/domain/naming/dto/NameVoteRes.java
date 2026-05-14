package com.team05.petmeeting.domain.naming.dto;
// 투표 DTO
public record NameVoteRes(
    Long candidateId,
    int currentVoteCount
) {
}
