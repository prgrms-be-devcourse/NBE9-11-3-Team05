package com.team05.petmeeting.domain.shelter.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShelter is a Querydsl query type for Shelter
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShelter extends EntityPathBase<Shelter> {

    private static final long serialVersionUID = -1322531590L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShelter shelter = new QShelter("shelter");

    public final ListPath<com.team05.petmeeting.domain.campaign.entity.Campaign, com.team05.petmeeting.domain.campaign.entity.QCampaign> campaigns = this.<com.team05.petmeeting.domain.campaign.entity.Campaign, com.team05.petmeeting.domain.campaign.entity.QCampaign>createList("campaigns", com.team05.petmeeting.domain.campaign.entity.Campaign.class, com.team05.petmeeting.domain.campaign.entity.QCampaign.class, PathInits.DIRECT2);

    public final StringPath careAddr = createString("careAddr");

    public final StringPath careNm = createString("careNm");

    public final StringPath careOwnerNm = createString("careOwnerNm");

    public final StringPath careRegNo = createString("careRegNo");

    public final StringPath careTel = createString("careTel");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath orgNm = createString("orgNm");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> updTm = createDateTime("updTm", java.time.LocalDateTime.class);

    public final com.team05.petmeeting.domain.user.entity.QUser user;

    public QShelter(String variable) {
        this(Shelter.class, forVariable(variable), INITS);
    }

    public QShelter(Path<? extends Shelter> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShelter(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShelter(PathMetadata metadata, PathInits inits) {
        this(Shelter.class, metadata, inits);
    }

    public QShelter(Class<? extends Shelter> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.team05.petmeeting.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

