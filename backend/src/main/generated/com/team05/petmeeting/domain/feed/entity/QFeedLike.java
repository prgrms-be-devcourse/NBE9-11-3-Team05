package com.team05.petmeeting.domain.feed.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeedLike is a Querydsl query type for FeedLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedLike extends EntityPathBase<FeedLike> {

    private static final long serialVersionUID = -2039314265L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeedLike feedLike = new QFeedLike("feedLike");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QFeed feed;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.team05.petmeeting.domain.user.entity.QUser user;

    public QFeedLike(String variable) {
        this(FeedLike.class, forVariable(variable), INITS);
    }

    public QFeedLike(Path<? extends FeedLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeedLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeedLike(PathMetadata metadata, PathInits inits) {
        this(FeedLike.class, metadata, inits);
    }

    public QFeedLike(Class<? extends FeedLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.feed = inits.isInitialized("feed") ? new QFeed(forProperty("feed"), inits.get("feed")) : null;
        this.user = inits.isInitialized("user") ? new com.team05.petmeeting.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

