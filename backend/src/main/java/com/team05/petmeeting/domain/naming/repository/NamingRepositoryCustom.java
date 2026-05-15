package com.team05.petmeeting.domain.naming.repository;

import com.team05.petmeeting.domain.naming.dto.NameCandidateRes;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface NamingRepositoryCustom {

    NameCandidateRes getCandidates(
            Long animalId,
            @Nullable Long userId
    );

    List<NameCandidateRes.CandidateDto> findAllQualifiedCandidatesByShelter(String careNm, int threshold);

}
