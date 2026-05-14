package com.team05.petmeeting.domain.donation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDonation is a Querydsl query type for Donation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDonation extends EntityPathBase<Donation> {

    private static final long serialVersionUID = 1337916504L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDonation donation = new QDonation("donation");

    public final com.team05.petmeeting.global.entity.QBaseEntity _super = new com.team05.petmeeting.global.entity.QBaseEntity(this);

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final com.team05.petmeeting.domain.campaign.entity.QCampaign campaign;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath paymentId = createString("paymentId");

    public final EnumPath<com.team05.petmeeting.domain.donation.enums.DonationStatus> status = createEnum("status", com.team05.petmeeting.domain.donation.enums.DonationStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.team05.petmeeting.domain.user.entity.QUser user;

    public QDonation(String variable) {
        this(Donation.class, forVariable(variable), INITS);
    }

    public QDonation(Path<? extends Donation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDonation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDonation(PathMetadata metadata, PathInits inits) {
        this(Donation.class, metadata, inits);
    }

    public QDonation(Class<? extends Donation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.campaign = inits.isInitialized("campaign") ? new com.team05.petmeeting.domain.campaign.entity.QCampaign(forProperty("campaign"), inits.get("campaign")) : null;
        this.user = inits.isInitialized("user") ? new com.team05.petmeeting.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

