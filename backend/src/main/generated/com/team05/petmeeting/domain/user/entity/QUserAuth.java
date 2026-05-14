package com.team05.petmeeting.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserAuth is a Querydsl query type for UserAuth
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserAuth extends EntityPathBase<UserAuth> {

    private static final long serialVersionUID = -1122833774L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserAuth userAuth = new QUserAuth("userAuth");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath password = createString("password");

    public final EnumPath<com.team05.petmeeting.domain.user.provider.Provider> provider = createEnum("provider", com.team05.petmeeting.domain.user.provider.Provider.class);

    public final StringPath providerId = createString("providerId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QUserAuth(String variable) {
        this(UserAuth.class, forVariable(variable), INITS);
    }

    public QUserAuth(Path<? extends UserAuth> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserAuth(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserAuth(PathMetadata metadata, PathInits inits) {
        this(UserAuth.class, metadata, inits);
    }

    public QUserAuth(Class<? extends UserAuth> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

