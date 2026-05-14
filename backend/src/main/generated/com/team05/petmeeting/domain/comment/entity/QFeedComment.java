package com.team05.petmeeting.domain.comment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeedComment is a Querydsl query type for FeedComment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedComment extends EntityPathBase<FeedComment> {

    private static final long serialVersionUID = 1235157180L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeedComment feedComment = new QFeedComment("feedComment");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.team05.petmeeting.domain.feed.entity.QFeed feed;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.team05.petmeeting.domain.user.entity.QUser user;

    public QFeedComment(String variable) {
        this(FeedComment.class, forVariable(variable), INITS);
    }

    public QFeedComment(Path<? extends FeedComment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeedComment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeedComment(PathMetadata metadata, PathInits inits) {
        this(FeedComment.class, metadata, inits);
    }

    public QFeedComment(Class<? extends FeedComment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.feed = inits.isInitialized("feed") ? new com.team05.petmeeting.domain.feed.entity.QFeed(forProperty("feed"), inits.get("feed")) : null;
        this.user = inits.isInitialized("user") ? new com.team05.petmeeting.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

