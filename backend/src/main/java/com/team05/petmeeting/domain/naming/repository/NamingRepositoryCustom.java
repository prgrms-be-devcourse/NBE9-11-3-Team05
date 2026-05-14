package com.team05.petmeeting.domain.naming.repository;

import com.team05.petmeeting.domain.naming.dto.NameCandidateRes;

import java.util.List;

public interface NamingRepositoryCustom {

    NameCandidateRes getCandidates(
            Long animalId,
            Long userId
    );

    List<NameCandidateRes.CandidateDto> findAllQualifiedCandidatesByShelter(String careNm, int threshold);

}
