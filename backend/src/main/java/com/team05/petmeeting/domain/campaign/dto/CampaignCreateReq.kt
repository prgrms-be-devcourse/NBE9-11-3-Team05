package com.team05.petmeeting.domain.campaign.dto

@JvmRecord
data class CampaignCreateReq(
    @JvmField val title: String?,
    @JvmField val description: String?,
    @JvmField val amount: Int
)
