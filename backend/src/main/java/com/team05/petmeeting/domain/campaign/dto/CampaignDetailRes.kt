package com.team05.petmeeting.domain.campaign.dto

import com.team05.petmeeting.domain.campaign.entity.Campaign
import com.team05.petmeeting.domain.campaign.enums.CampaignStatus

@JvmRecord
data class CampaignDetailRes(
    val campaignCount: Int,
    val campaigns: MutableList<CampaignDetailItem?>?
) {
    @JvmRecord
    data class CampaignDetailItem(
        val id: Long?,
        val title: String?,
        val targetAmount: Int,
        val currentAmount: Int,
        val status: CampaignStatus?
    ) {
        companion object {
            fun from(campaign: Campaign): CampaignDetailItem {
                return CampaignDetailItem(
                    campaign.getId(),
                    campaign.getTitle(),
                    campaign.getTargetAmount(),
                    campaign.getCurrentAmount(),
                    campaign.getStatus()
                )
            }
        }
    }

    companion object {
        fun from(campaign: MutableList<Campaign?>): CampaignDetailRes {
            val campaignDetailItems = campaign.stream()
                .map<CampaignDetailItem?> { campaign: Campaign? -> CampaignDetailItem.Companion.from(campaign!!) }
                .toList()
            return CampaignDetailRes(campaignDetailItems.size, campaignDetailItems)
        }
    }
}
