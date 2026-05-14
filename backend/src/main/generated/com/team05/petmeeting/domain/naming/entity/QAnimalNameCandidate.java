package com.team05.petmeeting.domain.naming.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAnimalNameCandidate is a Querydsl query type for AnimalNameCandidate
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAnimalNameCandidate extends EntityPathBase<AnimalNameCandidate> {

    private static final long serialVersionUID = -317877856L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAnimalNameCandidate animalNameCandidate = new QAnimalNameCandidate("animalNameCandidate");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    public final com.team05.petmeeting.domain.animal.entity.QAnimal animal;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isConfirmed = createBoolean("isConfirmed");

    public final StringPath proposedName = createString("proposedName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.team05.petmeeting.domain.user.entity.QUser user;

    public final NumberPath<Integer> voteCount = createNumber("voteCount", Integer.class);

    public QAnimalNameCandidate(String variable) {
        this(AnimalNameCandidate.class, forVariable(variable), INITS);
    }

    public QAnimalNameCandidate(Path<? extends AnimalNameCandidate> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAnimalNameCandidate(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAnimalNameCandidate(PathMetadata metadata, PathInits inits) {
        this(AnimalNameCandidate.class, metadata, inits);
    }

    public QAnimalNameCandidate(Class<? extends AnimalNameCandidate> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.animal = inits.isInitialized("animal") ? new com.team05.petmeeting.domain.animal.entity.QAnimal(forProperty("animal"), inits.get("animal")) : null;
        this.user = inits.isInitialized("user") ? new com.team05.petmeeting.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

