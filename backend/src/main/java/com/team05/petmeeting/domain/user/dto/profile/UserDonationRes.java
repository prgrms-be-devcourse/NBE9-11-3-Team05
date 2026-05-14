package com.team05.petmeeting.domain.user.dto.profile;

import com.team05.petmeeting.domain.donation.entity.Donation;
import com.team05.petmeeting.domain.donation.enums.DonationStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record UserDonationRes(
    int donationCount,
    int donationTotalAmount,
    List<UserDonationItem> donations
){
    public static UserDonationRes of(
            int donationCount,
            int donationTotalAmount,
            List<Donation> donations
    ){
        List<UserDonationItem> items = donations.stream()
                .map(UserDonationItem::from)
                .toList();
        return new UserDonationRes(donationCount, donationTotalAmount, items);
    }

    public record UserDonationItem(
            Long id,
            int amount,
            DonationStatus status,
            Long campaignId
    ){
        public static UserDonationItem from(Donation donation) {
            return new UserDonationItem(
                    donation.getId(),
                    donation.getAmount(),
                    donation.getStatus(),
                    donation.getCampaign().getId()
            );
        }
    }


}
