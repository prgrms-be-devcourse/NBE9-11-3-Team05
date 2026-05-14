package com.team05.petmeeting.domain.adoption.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdoptionApplication is a Querydsl query type for AdoptionApplication
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdoptionApplication extends EntityPathBase<AdoptionApplication> {

    private static final long serialVersionUID = -1574971188L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdoptionApplication adoptionApplication = new QAdoptionApplication("adoptionApplication");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    public final com.team05.petmeeting.domain.animal.entity.QAnimal animal;

    public final StringPath applyReason = createString("applyReason");

    public final StringPath applyTel = createString("applyTel");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath rejectionReason = createString("rejectionReason");

    public final DateTimePath<java.time.LocalDateTime> reviewedAt = createDateTime("reviewedAt", java.time.LocalDateTime.class);

    public final EnumPath<AdoptionStatus> status = createEnum("status", AdoptionStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.team05.petmeeting.domain.user.entity.QUser user;

    public QAdoptionApplication(String variable) {
        this(AdoptionApplication.class, forVariable(variable), INITS);
    }

    public QAdoptionApplication(Path<? extends AdoptionApplication> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdoptionApplication(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdoptionApplication(PathMetadata metadata, PathInits inits) {
        this(AdoptionApplication.class, metadata, inits);
    }

    public QAdoptionApplication(Class<? extends AdoptionApplication> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.animal = inits.isInitialized("animal") ? new com.team05.petmeeting.domain.animal.entity.QAnimal(forProperty("animal"), inits.get("animal")) : null;
        this.user = inits.isInitialized("user") ? new com.team05.petmeeting.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

