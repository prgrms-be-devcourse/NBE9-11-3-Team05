package com.team05.petmeeting.domain.campaign.dto;

import com.team05.petmeeting.domain.campaign.entity.Campaign;
import com.team05.petmeeting.domain.campaign.enums.CampaignStatus;

import java.util.List;

public record CampaignDetailRes (
        int campaignCount,
        List<CampaignDetailItem> campaigns
){
    public static CampaignDetailRes from(List<Campaign> campaign) {
        List<CampaignDetailItem> campaignDetailItems = campaign.stream()
                .map(CampaignDetailItem::from)
                .toList();
        return new CampaignDetailRes(campaignDetailItems.size(), campaignDetailItems);
    }
    public record CampaignDetailItem(
            Long id,
            String title,
            int targetAmount,
            int currentAmount,
            CampaignStatus status
    ){
        public static CampaignDetailItem from(Campaign campaign) {
            return new CampaignDetailItem(
                    campaign.getId(),
                    campaign.getTitle(),
                    campaign.getTargetAmount(),
                    campaign.getCurrentAmount(),
                    campaign.getStatus()
            );
        }
    }
}
