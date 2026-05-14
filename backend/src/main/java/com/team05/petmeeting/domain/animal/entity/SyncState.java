package com.team05.petmeeting.domain.animal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "animal_sync_states")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SyncState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_type", nullable = false, unique = true, length = 20)
    private AnimalSyncType syncType;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    private SyncState(AnimalSyncType syncType) {
        this.syncType = syncType;
    }

    public static SyncState create(AnimalSyncType syncType) {
        return new SyncState(syncType);
    }

    public void updateLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
