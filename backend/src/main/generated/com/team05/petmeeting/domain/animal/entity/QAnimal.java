package com.team05.petmeeting.domain.animal.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAnimal is a Querydsl query type for Animal
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAnimal extends EntityPathBase<Animal> {

    private static final long serialVersionUID = -1684088084L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAnimal animal = new QAnimal("animal");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    public final StringPath age = createString("age");

    public final DateTimePath<java.time.LocalDateTime> apiUpdatedAt = createDateTime("apiUpdatedAt", java.time.LocalDateTime.class);

    public final StringPath careAddr = createString("careAddr");

    public final StringPath careNm = createString("careNm");

    public final StringPath careOwnerNm = createString("careOwnerNm");

    public final StringPath careTel = createString("careTel");

    public final StringPath colorCd = createString("colorCd");

    public final ListPath<com.team05.petmeeting.domain.comment.entity.AnimalComment, com.team05.petmeeting.domain.comment.entity.QAnimalComment> comments = this.<com.team05.petmeeting.domain.comment.entity.AnimalComment, com.team05.petmeeting.domain.comment.entity.QAnimalComment>createList("comments", com.team05.petmeeting.domain.comment.entity.AnimalComment.class, com.team05.petmeeting.domain.comment.entity.QAnimalComment.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath desertionNo = createString("desertionNo");

    public final StringPath happenPlace = createString("happenPlace");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath kindFullNm = createString("kindFullNm");

    public final StringPath name = createString("name");

    public final DatePath<java.time.LocalDate> noticeEdt = createDate("noticeEdt", java.time.LocalDate.class);

    public final StringPath noticeNo = createString("noticeNo");

    public final StringPath popfile1 = createString("popfile1");

    public final StringPath popfile2 = createString("popfile2");

    public final StringPath processState = createString("processState");

    public final StringPath sexCd = createString("sexCd");

    public final com.team05.petmeeting.domain.shelter.entity.QShelter shelter;

    public final StringPath specialMark = createString("specialMark");

    public final NumberPath<Integer> stateGroup = createNumber("stateGroup", Integer.class);

    public final NumberPath<Integer> totalCheerCount = createNumber("totalCheerCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath upKindNm = createString("upKindNm");

    public final StringPath weight = createString("weight");

    public QAnimal(String variable) {
        this(Animal.class, forVariable(variable), INITS);
    }

    public QAnimal(Path<? extends Animal> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAnimal(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAnimal(PathMetadata metadata, PathInits inits) {
        this(Animal.class, metadata, inits);
    }

    public QAnimal(Class<? extends Animal> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.shelter = inits.isInitialized("shelter") ? new com.team05.petmeeting.domain.shelter.entity.QShelter(forProperty("shelter"), inits.get("shelter")) : null;
    }

}

