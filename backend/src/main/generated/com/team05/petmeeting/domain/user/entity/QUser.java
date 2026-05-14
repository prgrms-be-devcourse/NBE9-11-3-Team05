package com.team05.petmeeting.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -664564278L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> dailyHeartCount = createNumber("dailyHeartCount", Integer.class);

    public final StringPath email = createString("email");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final DatePath<java.time.LocalDate> lastHeartResetDate = createDate("lastHeartResetDate", java.time.LocalDate.class);

    public final StringPath nickname = createString("nickname");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final StringPath realname = createString("realname");

    public final EnumPath<com.team05.petmeeting.domain.user.role.Role> role = createEnum("role", com.team05.petmeeting.domain.user.role.Role.class);

    public final com.team05.petmeeting.domain.shelter.entity.QShelter shelter;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final ListPath<UserAuth, QUserAuth> userAuths = this.<UserAuth, QUserAuth>createList("userAuths", UserAuth.class, QUserAuth.class, PathInits.DIRECT2);

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.shelter = inits.isInitialized("shelter") ? new com.team05.petmeeting.domain.shelter.entity.QShelter(forProperty("shelter"), inits.get("shelter")) : null;
    }

}

