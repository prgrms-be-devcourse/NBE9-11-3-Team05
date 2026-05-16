package com.team05.petmeeting.domain.campaign.dto

import com.team05.petmeeting.domain.campaign.entity.Campaign
import com.team05.petmeeting.domain.campaign.enums.CampaignStatus

@JvmRecord
data class CampaignRes(
    val totalCampaigns: Int,
    val campaigns: MutableList<CampaignItem?>?
) {
    @JvmRecord
    private data class CampaignItem(
        val id: Long?,
        val title: String?,
        val description: String?,
        val targetAmount: Int,
        val currentAmount: Int,
        val status: CampaignStatus?,
        val shelterId: String?
    ) {
        companion object {
            fun from(campaign: Campaign): CampaignItem {
                return CampaignItem(
                    campaign.getId(),
                    campaign.getTitle(),
                    campaign.getDescription(),
                    campaign.getTargetAmount(),
                    campaign.getCurrentAmount(),
                    campaign.getStatus(),
                    campaign.getShelter().careRegNo
                )
            }
        }
    }

    companion object {
        fun of(totalCampaigns: Int, campaignList: MutableList<Campaign?>): CampaignRes {
            val items: MutableList<CampaignItem?> = campaignList.stream()
                .map<CampaignItem?> { campaign: Campaign? -> CampaignItem.Companion.from(campaign!!) }
                .toList()
            return CampaignRes(totalCampaigns, items)
        }
    }
}
