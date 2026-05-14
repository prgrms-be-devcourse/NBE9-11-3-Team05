package com.team05.petmeeting.domain.campaign.dto;

import com.team05.petmeeting.domain.campaign.enums.CampaignStatus;

public record CampaignCloseRes (
        Long id,
        CampaignStatus status
) {
}
