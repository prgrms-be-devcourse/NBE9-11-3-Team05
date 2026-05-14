package com.team05.petmeeting.domain.feed.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeed is a Querydsl query type for Feed
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeed extends EntityPathBase<Feed> {

    private static final long serialVersionUID = -1415160592L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeed feed = new QFeed("feed");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    public final com.team05.petmeeting.domain.animal.entity.QAnimal animal;

    public final EnumPath<com.team05.petmeeting.domain.feed.enums.FeedCategory> category = createEnum("category", com.team05.petmeeting.domain.feed.enums.FeedCategory.class);

    public final ListPath<com.team05.petmeeting.domain.comment.entity.FeedComment, com.team05.petmeeting.domain.comment.entity.QFeedComment> comments = this.<com.team05.petmeeting.domain.comment.entity.FeedComment, com.team05.petmeeting.domain.comment.entity.QFeedComment>createList("comments", com.team05.petmeeting.domain.comment.entity.FeedComment.class, com.team05.petmeeting.domain.comment.entity.QFeedComment.class, PathInits.DIRECT2);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.team05.petmeeting.domain.user.entity.QUser user;

    public QFeed(String variable) {
        this(Feed.class, forVariable(variable), INITS);
    }

    public QFeed(Path<? extends Feed> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeed(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeed(PathMetadata metadata, PathInits inits) {
        this(Feed.class, metadata, inits);
    }

    public QFeed(Class<? extends Feed> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.animal = inits.isInitialized("animal") ? new com.team05.petmeeting.domain.animal.entity.QAnimal(forProperty("animal"), inits.get("animal")) : null;
        this.user = inits.isInitialized("user") ? new com.team05.petmeeting.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

