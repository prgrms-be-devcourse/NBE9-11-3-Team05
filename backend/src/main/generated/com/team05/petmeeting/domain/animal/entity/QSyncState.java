package com.team05.petmeeting.domain.animal.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSyncState is a Querydsl query type for SyncState
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSyncState extends EntityPathBase<SyncState> {

    private static final long serialVersionUID = -1789825082L;

    public static final QSyncState syncState = new QSyncState("syncState");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastUpdatedAt = createDateTime("lastUpdatedAt", java.time.LocalDateTime.class);

    public final EnumPath<AnimalSyncType> syncType = createEnum("syncType", AnimalSyncType.class);

    public QSyncState(String variable) {
        super(SyncState.class, forVariable(variable));
    }

    public QSyncState(Path<? extends SyncState> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSyncState(PathMetadata metadata) {
        super(SyncState.class, metadata);
    }

}

