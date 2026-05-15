package com.team05.petmeeting.domain.animal.repository

import com.team05.petmeeting.domain.animal.entity.AnimalSyncType
import com.team05.petmeeting.domain.animal.entity.SyncState
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface SyncStateRepository : JpaRepository<SyncState, Long> {
    fun findBySyncType(syncType: AnimalSyncType): Optional<SyncState>
}
