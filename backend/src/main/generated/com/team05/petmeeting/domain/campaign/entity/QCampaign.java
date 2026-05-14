package com.team05.petmeeting.domain.campaign.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCampaign is a Querydsl query type for Campaign
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCampaign extends EntityPathBase<Campaign> {

    private static final long serialVersionUID = -662490284L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCampaign campaign = new QCampaign("campaign");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> currentAmount = createNumber("currentAmount", Integer.class);

    public final StringPath description = createString("description");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final com.team05.petmeeting.domain.shelter.entity.QShelter shelter;

    public final EnumPath<com.team05.petmeeting.domain.campaign.enums.CampaignStatus> status = createEnum("status", com.team05.petmeeting.domain.campaign.enums.CampaignStatus.class);

    public final NumberPath<Integer> targetAmount = createNumber("targetAmount", Integer.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QCampaign(String variable) {
        this(Campaign.class, forVariable(variable), INITS);
    }

    public QCampaign(Path<? extends Campaign> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCampaign(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCampaign(PathMetadata metadata, PathInits inits) {
        this(Campaign.class, metadata, inits);
    }

    public QCampaign(Class<? extends Campaign> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.shelter = inits.isInitialized("shelter") ? new com.team05.petmeeting.domain.shelter.entity.QShelter(forProperty("shelter"), inits.get("shelter")) : null;
    }

}

