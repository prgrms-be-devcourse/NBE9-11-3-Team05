package com.team05.petmeeting.domain.donation.entity

import com.team05.petmeeting.domain.campaign.entity.Campaign
import com.team05.petmeeting.domain.donation.enums.DonationStatus
import com.team05.petmeeting.domain.user.entity.User
import com.team05.petmeeting.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "donations")
class Donation : BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    var campaign: Campaign? = null

    @Column(unique = true)
    var paymentId: String? = null

    var amount: Int = 0

    @Enumerated(EnumType.STRING)
    var status: DonationStatus = DonationStatus.PENDING
        private set

    constructor(
        user: User?, campaign: Campaign?,
        paymentId: String?, amount: Int
    ) {
        this.user = user
        this.campaign = campaign
        this.paymentId = paymentId
        this.amount = amount
    }

    protected constructor()

    fun complete(paymentId: String?) {
        this.paymentId = paymentId
        this.status = DonationStatus.PAID
    }

    fun fail() {
        this.status = DonationStatus.FAILED
    }

    private class DonationBuilder {
        private var user: User? = null
        private var campaign: Campaign? = null
        private var paymentId: String? = null
        private var amount = 0
        fun user(user: User?): DonationBuilder {
            this.user = user
            return this
        }

        fun campaign(campaign: Campaign?): DonationBuilder {
            this.campaign = campaign
            return this
        }

        fun paymentId(paymentId: String?): DonationBuilder {
            this.paymentId = paymentId
            return this
        }

        fun amount(amount: Int): DonationBuilder {
            this.amount = amount
            return this
        }

        fun build(): Donation {
            return Donation(this.user, this.campaign, this.paymentId, this.amount)
        }

        override fun toString(): String {
            return "Donation.DonationBuilder(user=" + this.user + ", campaign=" + this.campaign + ", paymentId=" + this.paymentId + ", amount=" + this.amount + ")"
        }
    }

    companion object {
        fun create(user: User?, campaign: Campaign?, paymentId: String?, amount: Int): Donation {
            return Donation(
                user,
                campaign,
                paymentId,
                amount
            )
        }

        private fun builder(): DonationBuilder {
            return DonationBuilder()
        }
    }
}
