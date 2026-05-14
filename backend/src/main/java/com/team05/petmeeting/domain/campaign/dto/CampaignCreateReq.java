package com.team05.petmeeting.domain.campaign.dto;

public record CampaignCreateReq(
        String title,
        String description,
        int amount
){
}
