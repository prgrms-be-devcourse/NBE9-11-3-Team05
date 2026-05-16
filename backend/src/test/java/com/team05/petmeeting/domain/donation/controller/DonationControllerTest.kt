package com.team05.petmeeting.domain.donation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.team05.petmeeting.domain.donation.dto.CompleteReq
import com.team05.petmeeting.domain.donation.dto.CompleteRes
import com.team05.petmeeting.domain.donation.dto.PrepareReq
import com.team05.petmeeting.domain.donation.dto.PrepareRes
import com.team05.petmeeting.domain.donation.enums.DonationStatus
import com.team05.petmeeting.domain.donation.service.DonationService
import com.team05.petmeeting.global.security.test.WithCustomUser
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
    controllers = [DonationController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = ["com\\.team05\\.petmeeting\\.global\\.security\\..*"]
    )]
)
@WithCustomUser(userId = 100L)
class DonationControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    private val objectMapper = ObjectMapper()

    @MockitoBean
    private lateinit var donationService: DonationService

    @Test
    @DisplayName("결제 준비 성공")
    fun prepareDonation_success() {
        val req = PrepareReq(1L, 10000)
        val res = PrepareRes("payment-123", 10000)

        whenever(donationService.prepare(eq(100L), any())).thenReturn(res)

        mvc.perform(
            post("/api/v1/donations/prepare")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value("payment-123"))
            .andExpect(jsonPath("$.amount").value(10000))
    }

    @Test
    @DisplayName("결제 완료 성공")
    fun completeDonation_success() {
        val req = CompleteReq("payment-123")
        val res = CompleteRes(1L, 10000, DonationStatus.PAID, 1L)

        whenever(donationService.donate(eq(100L), any())).thenReturn(res)

        mvc.perform(
            post("/api/v1/donations/complete")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.amount").value(10000))
            .andExpect(jsonPath("$.status").value("PAID"))
            .andExpect(jsonPath("$.campaignId").value(1L))
    }
}