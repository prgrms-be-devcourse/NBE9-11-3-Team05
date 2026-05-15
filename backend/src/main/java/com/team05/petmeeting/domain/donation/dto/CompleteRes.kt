package com.team05.petmeeting.domain.donation.dto

import com.team05.petmeeting.domain.donation.enums.DonationStatus
import lombok.Builder

@Builder
@JvmRecord
data class CompleteRes(
    val id: Long?,
    val amount: Int,
    val status: DonationStatus?,
    val campaignId: Long?
) {
    companion object {
        fun create(): CompleteRes {
            return CompleteRes(1L, 10000, DonationStatus.PENDING, 1L)
        }
    }
}
