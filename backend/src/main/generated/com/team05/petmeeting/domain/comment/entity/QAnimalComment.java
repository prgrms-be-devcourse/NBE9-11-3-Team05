package com.team05.petmeeting.domain.comment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAnimalComment is a Querydsl query type for AnimalComment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAnimalComment extends EntityPathBase<AnimalComment> {

    private static final long serialVersionUID = 1664010206L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAnimalComment animalComment = new QAnimalComment("animalComment");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    public final com.team05.petmeeting.domain.animal.entity.QAnimal animal;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.team05.petmeeting.domain.user.entity.QUser user;

    public QAnimalComment(String variable) {
        this(AnimalComment.class, forVariable(variable), INITS);
    }

    public QAnimalComment(Path<? extends AnimalComment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAnimalComment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAnimalComment(PathMetadata metadata, PathInits inits) {
        this(AnimalComment.class, metadata, inits);
    }

    public QAnimalComment(Class<? extends AnimalComment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.animal = inits.isInitialized("animal") ? new com.team05.petmeeting.domain.animal.entity.QAnimal(forProperty("animal"), inits.get("animal")) : null;
        this.user = inits.isInitialized("user") ? new com.team05.petmeeting.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

