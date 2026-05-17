package com.team05.petmeeting.domain.donation.service

import com.team05.petmeeting.domain.campaign.service.CampaignService
import com.team05.petmeeting.domain.donation.dto.CompleteReq
import com.team05.petmeeting.domain.donation.dto.CompleteRes
import com.team05.petmeeting.domain.donation.dto.CompleteRes.Companion.create
import com.team05.petmeeting.domain.donation.dto.PrepareReq
import com.team05.petmeeting.domain.donation.dto.PrepareRes
import com.team05.petmeeting.domain.donation.entity.Donation
import com.team05.petmeeting.domain.donation.enums.DonationStatus
import com.team05.petmeeting.domain.donation.errorCode.DonationErrorCode
import com.team05.petmeeting.domain.donation.repository.DonationRepository
import com.team05.petmeeting.domain.user.dto.profile.UserDonationRes
import com.team05.petmeeting.domain.user.service.UserService
import com.team05.petmeeting.global.exception.BusinessException
import io.portone.sdk.server.errors.*
import io.portone.sdk.server.payment.PaidPayment
import io.portone.sdk.server.payment.Payment
import io.portone.sdk.server.payment.PaymentClient
import jakarta.transaction.Transactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
@Transactional
class DonationService(
    private val donationRepository: DonationRepository,
    private val campaignService: CampaignService,
    private val userService: UserService,
    private val paymentClient: PaymentClient
) {
    @Value("\${portone.store-id}")
    private val storeId: String? = null

    // 결제 준비 paymentId 발급
    fun prepare(userId: Long, req: PrepareReq): PrepareRes {
        val paymentId = "payment-" + UUID.randomUUID()
        val user = userService.findById(userId)
        val campaign = campaignService.findById(req.campaignId)

        val donation = Donation.create(user, campaign, paymentId, req.amount)
        donationRepository.save(donation)

        return PrepareRes(paymentId, req.amount)
    }

    // 결제 완료 + 검증
    // paymentClient의 getPayment 함수가 asynchronous 비동기여서 suspend여야함
    suspend fun donate(userId: Long, req: CompleteReq): CompleteRes {
        // todo : 검증 로직
        val donation = withContext(Dispatchers.IO) {    // db 조회는 blocking(동기)이고 donate 함수는 suspend (비동기)여서 IO용 스레드풀에서 따로 실행하도록
            donationRepository.findByPaymentId(req.paymentId)
        }

        // 포트원에서 결제 정보 불러오기
        val payment = try {
            paymentClient.getPayment(req.paymentId)
        } catch (error: PortOneException) {
            if (error is GetPaymentException) {
                when (error) {
                    is PaymentNotFoundException -> throw BusinessException(DonationErrorCode.PAYMENT_NOT_FOUND)
                    is UnauthorizedException -> throw BusinessException(DonationErrorCode.UNAUTHORIZED)
                    is ForbiddenException -> throw BusinessException(DonationErrorCode.FORBIDDEN)
                    is InvalidRequestException -> throw BusinessException(DonationErrorCode.INVALID_REQUEST)
                    is UnknownException -> throw BusinessException(DonationErrorCode.UNKNOWN)
                }
            }
            throw BusinessException(DonationErrorCode.PAYMENT_NOT_FOUND)
        }

        // SDK가 지원하는 응답인지 확인
        if (payment !is Payment.Recognized) {
            throw BusinessException(DonationErrorCode.PAYMENT_NOT_FOUND)
        }

        // 결제 완료인지 확인
        if (payment !is PaidPayment) {
            throw BusinessException(DonationErrorCode.PAYMENT_NOT_PAID)
        }




//        val payment = try {
//            portOne.payment.getPaymentFuture(req.paymentId)
//        } catch (e: Exception) {
//            throw RuntimeException(e.message)
//        }
//
//        if (payment !is PaidPayment){
//
//        }
//
//        val paidAmount = (payment as PaidPayment).amount.total.toInt()
//        if (paidAmount != donation.amount) {
//            donation.fail()
//            throw IllegalStateException("결제 금액 불일치")
//        }

        return create()
    }

    // 웹훅 처리
    fun handleWebhook(paymentId: String) {
        // todo : webhook 검증 로직
    }

    // 내 후원 내역
    fun getMyDonations(userId: Long): UserDonationRes {
        val donations = donationRepository.findByUser_Id(userId)
        val totalAmount = donations.stream()
            .filter { d: Donation -> d.status == DonationStatus.PAID }
            .mapToInt { obj: Donation -> obj.amount }
            .sum()
        return UserDonationRes.of(donations.size, totalAmount, donations)
    }
}
