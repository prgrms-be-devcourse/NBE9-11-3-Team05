package com.team05.petmeeting.domain.animal.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "animal_sync_states")
class SyncState protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_type", nullable = false, unique = true, length = 20)
    lateinit var syncType: AnimalSyncType
        protected set

    @Column(name = "last_updated_at")
    var lastUpdatedAt: LocalDateTime? = null
        protected set

    private constructor(syncType: AnimalSyncType) : this() {
        this.syncType = syncType
    }

    fun updateLastUpdatedAt(lastUpdatedAt: LocalDateTime) {
        this.lastUpdatedAt = lastUpdatedAt
    }

    companion object {
        @JvmStatic
        fun create(syncType: AnimalSyncType): SyncState = SyncState(syncType)
    }
}
