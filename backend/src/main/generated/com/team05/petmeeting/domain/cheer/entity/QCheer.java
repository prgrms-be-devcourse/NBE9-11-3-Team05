package com.team05.petmeeting.domain.cheer.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCheer is a Querydsl query type for Cheer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCheer extends EntityPathBase<Cheer> {

    private static final long serialVersionUID = -985597702L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCheer cheer = new QCheer("cheer");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    public final com.team05.petmeeting.domain.animal.entity.QAnimal animal;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.team05.petmeeting.domain.user.entity.QUser user;

    public QCheer(String variable) {
        this(Cheer.class, forVariable(variable), INITS);
    }

    public QCheer(Path<? extends Cheer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCheer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCheer(PathMetadata metadata, PathInits inits) {
        this(Cheer.class, metadata, inits);
    }

    public QCheer(Class<? extends Cheer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.animal = inits.isInitialized("animal") ? new com.team05.petmeeting.domain.animal.entity.QAnimal(forProperty("animal"), inits.get("animal")) : null;
        this.user = inits.isInitialized("user") ? new com.team05.petmeeting.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

