package com.team05.petmeeting.domain.donation.service

import com.team05.petmeeting.domain.campaign.service.CampaignService
import com.team05.petmeeting.domain.donation.dto.CompleteReq
import com.team05.petmeeting.domain.donation.dto.CompleteRes
import com.team05.petmeeting.domain.donation.dto.CompleteRes.Companion.create
import com.team05.petmeeting.domain.donation.dto.PrepareReq
import com.team05.petmeeting.domain.donation.dto.PrepareRes
import com.team05.petmeeting.domain.donation.entity.Donation
import com.team05.petmeeting.domain.donation.enums.DonationStatus
import com.team05.petmeeting.domain.donation.repository.DonationRepository
import com.team05.petmeeting.domain.user.dto.profile.UserDonationRes
import com.team05.petmeeting.domain.user.service.UserService
import io.portone.sdk.server.PortOneClient
import io.portone.sdk.server.payment.PaidPayment
import io.portone.sdk.server.payment.Payment
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
@Transactional
class DonationService(
    private val donationRepository: DonationRepository,
    private val campaignService: CampaignService,
    private val userService: UserService,
    private val portOne: PortOneClient
) {
    @Value("\${portone.store-id}")
    private val storeId: String? = null

    // 결제 준비 paymentId 발급
    fun prepare(userId: Long?, req: PrepareReq): PrepareRes {
        val paymentId = "payment-" + UUID.randomUUID()
        val user = userService.findById(userId)
        val campaign = campaignService.findById(req.campaignId)

        val donation = Donation.create(user, campaign, paymentId, req.amount)
        donationRepository.save<Donation?>(donation)

        return PrepareRes(paymentId, req.amount)
    }

    // 결제 완료 + 검증
    fun donate(userId: Long?, req: CompleteReq): CompleteRes {
        // todo : 검증 로직
        val donation = donationRepository.findByPaymentId(req.paymentId)
        val payment: Payment? = null

        try {
            portOne.payment.getPaymentFuture(req.paymentId)
        } catch (e: Exception) {
            throw RuntimeException(e.message)
        }
        val paidAmount = (payment as PaidPayment).amount.total.toInt()
        if (paidAmount != donation.getAmount()) {
            donation.fail()
            throw IllegalStateException("결제 금액 불일치")
        }

        return create()
    }

    // 웹훅 처리
    fun handleWebhook(paymentId: String?) {
        // todo : webhook 검증 로직
    }

    // 내 후원 내역
    fun getMyDonations(userId: Long?): UserDonationRes {
        val donations = donationRepository.findByUser_Id(userId)
        val totalAmount = donations.stream()
            .filter { d: Donation? -> d!!.getStatus() == DonationStatus.PAID }
            .mapToInt { obj: Donation? -> obj!!.getAmount() }
            .sum()
        return UserDonationRes.of(donations.size, totalAmount, donations)
    }
}
