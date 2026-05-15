package com.team05.petmeeting.domain.donation.dto

@JvmRecord
data class PrepareReq(
    val campaignId: Long?,
    val amount: Int
)
