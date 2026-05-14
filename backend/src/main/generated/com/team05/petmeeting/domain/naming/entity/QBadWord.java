package com.team05.petmeeting.domain.naming.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBadWord is a Querydsl query type for BadWord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBadWord extends EntityPathBase<BadWord> {

    private static final long serialVersionUID = -1403453005L;

    public static final QBadWord badWord = new QBadWord("badWord");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath word = createString("word");

    public QBadWord(String variable) {
        super(BadWord.class, forVariable(variable));
    }

    public QBadWord(Path<? extends BadWord> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBadWord(PathMetadata metadata) {
        super(BadWord.class, metadata);
    }

}

