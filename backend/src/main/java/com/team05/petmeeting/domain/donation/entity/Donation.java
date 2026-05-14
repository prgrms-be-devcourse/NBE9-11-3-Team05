package com.team05.petmeeting.domain.donation.entity;

import com.team05.petmeeting.domain.campaign.entity.Campaign;
import com.team05.petmeeting.domain.donation.enums.DonationStatus;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="donations")
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class Donation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="campaign_id")
    Campaign campaign;

    @Column(unique = true)
    String paymentId;

    int amount;

    @Enumerated(EnumType.STRING)
    private DonationStatus status = DonationStatus.PENDING;

    @Builder(access = AccessLevel.PRIVATE)
    public Donation(User user, Campaign campaign,
                    String paymentId, int amount) {
        this.user = user;
        this.campaign = campaign;
        this.paymentId = paymentId;
        this.amount = amount;
    }

    public static Donation create(User user, Campaign campaign, String paymentId, int amount) {
        return new Donation(
                user,
                campaign,
                paymentId,
                amount
        );
    }

    public void complete(String paymentId) {
        this.paymentId = paymentId;
        this.status = DonationStatus.PAID;
    }

    public void fail() {
        this.status = DonationStatus.FAILED;
    }
}
