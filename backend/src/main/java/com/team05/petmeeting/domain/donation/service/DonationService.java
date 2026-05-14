package com.team05.petmeeting.domain.donation.service;

import com.team05.petmeeting.domain.campaign.entity.Campaign;
import com.team05.petmeeting.domain.campaign.service.CampaignService;
import com.team05.petmeeting.domain.donation.dto.CompleteReq;
import com.team05.petmeeting.domain.donation.dto.CompleteRes;
import com.team05.petmeeting.domain.donation.dto.PrepareReq;
import com.team05.petmeeting.domain.donation.dto.PrepareRes;
import com.team05.petmeeting.domain.donation.entity.Donation;
import com.team05.petmeeting.domain.donation.enums.DonationStatus;
import com.team05.petmeeting.domain.donation.repository.DonationRepository;
import com.team05.petmeeting.domain.user.dto.profile.UserDonationRes;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.service.UserService;
import io.portone.sdk.server.PortOneClient;
import io.portone.sdk.server.payment.PaidPayment;
import io.portone.sdk.server.payment.Payment;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DonationService {
    private final DonationRepository donationRepository;
    private final CampaignService campaignService;
    private final UserService userService;
    private final PortOneClient portOne;

    @Value("${portone.store-id}")
    private String storeId;

    // 결제 준비 paymentId 발급
    public PrepareRes prepare(Long userId, PrepareReq req) {
        String paymentId = "payment-" + UUID.randomUUID();
        User user = userService.findById(userId);
        Campaign campaign = campaignService.findById(req.campaignId());

        Donation donation = Donation.create(user, campaign, paymentId, req.amount());
        donationRepository.save(donation);

        return new PrepareRes(paymentId, req.amount());
    }

    // 결제 완료 + 검증
    public CompleteRes donate(Long userId, CompleteReq req) {
        // todo : 검증 로직
        Donation donation = donationRepository.findByPaymentId(req.paymentId());
        Payment payment = null;

        try {portOne.getPayment().getPayment(req.paymentId());}
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        int paidAmount = (int)((PaidPayment) payment).getAmount().getTotal();
        if (paidAmount != donation.getAmount()) {
            donation.fail();
            throw new IllegalStateException("결제 금액 불일치");
        }

        return CompleteRes.create();
    }

    // 웹훅 처리
    public void handleWebhook(String paymentId) {
        // todo : webhook 검증 로직
    }

    // 내 후원 내역
    public UserDonationRes getMyDonations(Long userId){
        List<Donation> donations = donationRepository.findByUser_Id(userId);
        int totalAmount = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.PAID)
                .mapToInt(Donation::getAmount)
                .sum();
        return UserDonationRes.of(donations.size(), totalAmount, donations);
    }

}
