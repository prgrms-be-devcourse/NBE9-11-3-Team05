package com.team05.petmeeting.domain.naming.repository;

import com.team05.petmeeting.domain.naming.entity.NameVoteHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NameVoteHistoryRepository extends JpaRepository<NameVoteHistory, Long> {
    Boolean existsByUserIdAndAnimalId(Long userId, Long animalId);
}
