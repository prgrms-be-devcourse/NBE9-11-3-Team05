package com.team05.petmeeting.domain.user.dto.profile

import com.team05.petmeeting.domain.donation.entity.Donation
import com.team05.petmeeting.domain.donation.enums.DonationStatus

data class UserDonationRes(
    val donationCount: Int,
    val donationTotalAmount: Int,
    val donations: MutableList<UserDonationItem>
) {

    data class UserDonationItem(
        val id: Long,
        val amount: Int,
        val status: DonationStatus,
        val campaignId: Long
    ) {
        companion object {
            @JvmStatic
            fun from(donation: Donation): UserDonationItem {
                return UserDonationItem(
                    donation.id,
                    donation.amount,
                    donation.status,
                    donation.campaign.id
                )
            }
        }
    }

    companion object {
        @JvmStatic
        fun of(
            donationCount: Int,
            donationTotalAmount: Int,
            donations: MutableList<Donation>
        ): UserDonationRes {
            val items = donations.stream()
                .map { donation: Donation -> UserDonationItem.from(donation) }
                .toList()
            return UserDonationRes(donationCount, donationTotalAmount, items)
        }
    }
}
