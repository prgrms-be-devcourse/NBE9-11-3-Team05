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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(
    controllers = [DonationController::class],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.team05\\.petmeeting\\.global\\.security\\..*"
    )]
)
@WithCustomUser(userId = 100L) // 이미 로그인 된 사용자 만들어주는 것. api 사용하는 건 인증 된 사용자여야 해서 필요함..
class DonationControllerTest {
    @Autowired
    private val mvc: MockMvc? = null

    // @Autowired
    // private ObjectMapper objectMapper;
    // JacksonConfig.java   ← 이게 ObjectMapper 빈을 등록하고 있을 것
    // 근데 global/security 파일들 빈으로 로딩 안 해서 수동으로 만들어줘야 함
    private val objectMapper = ObjectMapper()

    @MockitoBean
    private val donationService: DonationService? = null

    @Test
    @DisplayName("결제 준비 성공")
    @Throws(Exception::class)
    fun prepareDonation_success() {
        val req = PrepareReq(1L, 10000)
        val res = PrepareRes("payment-123", 10000)

        Mockito.`when`<PrepareRes>(
            donationService!!.prepare(
                ArgumentMatchers.eq(100L),
                ArgumentMatchers.any<PrepareReq?>(PrepareReq::class.java)
            )
        ).thenReturn(res)

        mvc!!.perform(
            MockMvcRequestBuilders.post("/api/v1/donations/prepare")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.paymentId").value("payment-123"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(10000))
    }

    @Test
    @DisplayName("결제 완료 성공")
    @Throws(Exception::class)
    fun completeDonation_success() {
        val req = CompleteReq("payment-123")
        val res = CompleteRes(1L, 10000, DonationStatus.PAID, 1L)

        Mockito.`when`<CompleteRes>(
            donationService!!.donate(
                ArgumentMatchers.eq(100L),
                ArgumentMatchers.any<CompleteReq?>(CompleteReq::class.java)
            )
        ).thenReturn(res)

        mvc!!.perform(
            MockMvcRequestBuilders.post("/api/v1/donations/complete")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
            .andExpect(MockMvcResultMatchers.jsonPath("$.amount").value(10000))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PAID"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.campaignId").value(1L))
    }
}
