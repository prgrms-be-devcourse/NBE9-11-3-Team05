package com.team05.petmeeting.domain.campaign.dto;

import com.team05.petmeeting.domain.campaign.entity.Campaign;
import com.team05.petmeeting.domain.campaign.enums.CampaignStatus;

import java.util.List;

public record CampaignRes (
        int totalCampaigns,
        List<CampaignItem> campaigns
){
    public static CampaignRes of (int totalCampaigns, List<Campaign> campaignList){
        List<CampaignItem> items = campaignList.stream()
                .map(CampaignItem::from)
                .toList();
        return new CampaignRes(totalCampaigns, items);
    }
    private record CampaignItem(
        Long id,
        String title,
        String description,
        int targetAmount,
        int currentAmount,
        CampaignStatus status,
        String shelterId
    ){
        public static CampaignItem from(Campaign campaign){
            return new CampaignItem(
                    campaign.getId(),
                    campaign.getTitle(),
                    campaign.getDescription(),
                    campaign.getTargetAmount(),
                    campaign.getCurrentAmount(),
                    campaign.getStatus(),
                    campaign.getShelter().getCareRegNo()
            );
        }
    }
}
