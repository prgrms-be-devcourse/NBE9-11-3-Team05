package com.team05.petmeeting.domain.naming.dto;

// 이름 제안 DTO
public record NameProposalRes(
        Long candidateId,
        String proposedName
) {
}
