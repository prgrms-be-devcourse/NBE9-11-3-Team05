package com.team05.petmeeting.domain.campaign.dto

import com.team05.petmeeting.domain.campaign.entity.Campaign
import com.team05.petmeeting.domain.campaign.enums.CampaignStatus

data class CampaignCreateRes(
    val id: Long,
    val title: String,
    val targetAmount: Int,
    val status: CampaignStatus
) {
    companion object {
        @JvmStatic
        fun from(campaign: Campaign): CampaignCreateRes {
            return CampaignCreateRes(
                campaign.getId(),
                campaign.getTitle(),
                campaign.getTargetAmount(),
                campaign.getStatus()
            )
        }
    }
}
