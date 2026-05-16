package com.team05.petmeeting.domain.campaign.dto

import com.team05.petmeeting.domain.campaign.entity.Campaign
import com.team05.petmeeting.domain.campaign.enums.CampaignStatus

data class CampaignRes(
    val totalCampaigns: Int,
    val campaigns: List<CampaignItem>
) {
    data class CampaignItem(
        val id: Long,
        val title: String,
        val description: String,
        val targetAmount: Int,
        val currentAmount: Int,
        val status: CampaignStatus,
        val shelterId: String
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
        @JvmStatic
        fun of(totalCampaigns: Int, campaignList: List<Campaign>): CampaignRes {
            val items: List<CampaignItem> = campaignList.stream()
                .map{ campaign: Campaign -> CampaignItem.from(campaign) }
                .toList()
            return CampaignRes(totalCampaigns, items)
        }
    }
}
