package com.team05.petmeeting.domain.campaign.dto

data class CampaignCreateReq(
    val title: String,
    val description: String,
    val amount: Int
)
