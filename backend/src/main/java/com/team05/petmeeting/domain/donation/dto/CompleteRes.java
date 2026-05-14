package com.team05.petmeeting.domain.donation.dto;

import com.team05.petmeeting.domain.donation.enums.DonationStatus;
import lombok.Builder;

@Builder
public record CompleteRes (
    Long id,
    int amount,
    DonationStatus status,
    Long campaignId
){
    public static CompleteRes create() {
        return new CompleteRes(1L, 10000, DonationStatus.PENDING, 1L);

    }
}
