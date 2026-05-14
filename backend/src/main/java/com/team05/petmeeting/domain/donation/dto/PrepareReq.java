package com.team05.petmeeting.domain.donation.dto;

public record PrepareReq (
        Long campaignId,
        int amount
) {
}
