package com.team05.petmeeting.domain.animal.repository;

import com.team05.petmeeting.domain.animal.entity.AnimalSyncType;
import com.team05.petmeeting.domain.animal.entity.SyncState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SyncStateRepository extends JpaRepository<SyncState, Long> {
    Optional<SyncState> findBySyncType(AnimalSyncType syncType);
}
