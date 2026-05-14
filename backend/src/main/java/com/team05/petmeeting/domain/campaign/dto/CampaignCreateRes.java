package com.team05.petmeeting.domain.campaign.dto;

import com.team05.petmeeting.domain.campaign.entity.Campaign;
import com.team05.petmeeting.domain.campaign.enums.CampaignStatus;

public record CampaignCreateRes(
        Long id,
        String title,
        int targetAmount,
        CampaignStatus status
){
    public static CampaignCreateRes from(Campaign campaign) {
        return new CampaignCreateRes(
                campaign.getId(),
                campaign.getTitle(),
                campaign.getTargetAmount(),
                campaign.getStatus()
        );
    }
}
