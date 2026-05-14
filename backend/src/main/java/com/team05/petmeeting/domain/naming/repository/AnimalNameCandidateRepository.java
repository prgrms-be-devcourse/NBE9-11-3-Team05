package com.team05.petmeeting.domain.naming.repository;

import com.team05.petmeeting.domain.naming.entity.AnimalNameCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnimalNameCandidateRepository extends JpaRepository<AnimalNameCandidate, Long>, NamingRepositoryCustom {
    Optional<AnimalNameCandidate> findByAnimalIdAndProposedName(Long animalId, String proposedName);
}
