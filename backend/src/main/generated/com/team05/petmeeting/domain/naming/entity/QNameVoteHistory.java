package com.team05.petmeeting.domain.naming.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNameVoteHistory is a Querydsl query type for NameVoteHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNameVoteHistory extends EntityPathBase<NameVoteHistory> {

    private static final long serialVersionUID = -1415497789L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNameVoteHistory nameVoteHistory = new QNameVoteHistory("nameVoteHistory");

    public final com.team05.petmeeting.domain.animal.entity.QAnimal animal;

    public final QAnimalNameCandidate candidate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.team05.petmeeting.domain.user.entity.QUser user;

    public final DateTimePath<java.time.LocalDateTime> votedAt = createDateTime("votedAt", java.time.LocalDateTime.class);

    public QNameVoteHistory(String variable) {
        this(NameVoteHistory.class, forVariable(variable), INITS);
    }

    public QNameVoteHistory(Path<? extends NameVoteHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNameVoteHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNameVoteHistory(PathMetadata metadata, PathInits inits) {
        this(NameVoteHistory.class, metadata, inits);
    }

    public QNameVoteHistory(Class<? extends NameVoteHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.animal = inits.isInitialized("animal") ? new com.team05.petmeeting.domain.animal.entity.QAnimal(forProperty("animal"), inits.get("animal")) : null;
        this.candidate = inits.isInitialized("candidate") ? new QAnimalNameCandidate(forProperty("candidate"), inits.get("candidate")) : null;
        this.user = inits.isInitialized("user") ? new com.team05.petmeeting.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

