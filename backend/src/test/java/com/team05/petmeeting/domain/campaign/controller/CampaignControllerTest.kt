package com.team05.petmeeting.domain.campaign.controller

import com.team05.petmeeting.domain.campaign.dto.CampaignCreateReq
import com.team05.petmeeting.domain.campaign.dto.CampaignCreateRes
import com.team05.petmeeting.domain.campaign.dto.CampaignDetailRes
import com.team05.petmeeting.domain.campaign.dto.CampaignDetailRes.CampaignDetailItem
import com.team05.petmeeting.domain.campaign.dto.CampaignRes
import com.team05.petmeeting.domain.campaign.dto.CampaignRes.CampaignItem
import com.team05.petmeeting.domain.campaign.enums.CampaignStatus
import com.team05.petmeeting.domain.campaign.errorCode.CampaignErrorCode
import com.team05.petmeeting.domain.campaign.service.CampaignService
import com.team05.petmeeting.global.exception.BusinessException
import com.team05.petmeeting.global.security.test.WithCustomUser
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithCustomUser(userId = 100L)
internal class CampaignControllerTest {
    @Autowired
    private val mockMvc: MockMvc? = null

    @MockitoBean
    private val campaignService: CampaignService? = null

    @Autowired
    private val objectMapper: ObjectMapper? = null

    // 전체 캠페인 조회 성공
    @Test
    @Throws(Exception::class)
    fun getCampaigns_success() {
        val res = CampaignRes(0, mutableListOf<CampaignItem>())
        Mockito.`when`<CampaignRes>(campaignService!!.allCampaigns).thenReturn(res)

        mockMvc!!.perform(MockMvcRequestBuilders.get("/api/v1/campaigns"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalCampaigns").value(0))
    }

    // 전체 캠페인 조회 - 비로그인도 가능
    @Test
    @WithAnonymousUser
    @Throws(Exception::class)
    fun getCampaigns_anonymous_success() {
        val res = CampaignRes(0, mutableListOf<CampaignItem>())
        Mockito.`when`<CampaignRes>(campaignService!!.allCampaigns).thenReturn(res)

        mockMvc!!.perform(MockMvcRequestBuilders.get("/api/v1/campaigns"))
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    // 보호소 캠페인 생성 성공
    @Test
    @Throws(Exception::class)
    fun createCampaign_success() {
        val req = CampaignCreateReq("테스트 캠페인", "설명", 100000)
        val res = CampaignCreateRes(1L, "테스트 캠페인", 100000, CampaignStatus.ACTIVE)
        Mockito.`when`<CampaignCreateRes>(campaignService!!.createCampaign("shelter-001", 100L, req)).thenReturn(res)

        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/api/v1/shelters/shelter-001/campaign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper!!.writeValueAsString(req))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("테스트 캠페인"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.targetAmount").value(100000))
    }

    // 캠페인 생성 실패 - 비로그인 (401)
    @Test
    @WithAnonymousUser
    @Throws(Exception::class)
    fun createCampaign_fail_unauthorized() {
        val req = CampaignCreateReq("테스트 캠페인", "설명", 100000)

        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/api/v1/shelters/shelter-001/campaign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper!!.writeValueAsString(req))
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
    }

    // 캠페인 생성 실패 - 권한 없는 유저 (403)
    @Test
    @Throws(Exception::class)
    fun createCampaign_fail_forbidden() {
        val req = CampaignCreateReq("테스트 캠페인", "설명", 100000)
        Mockito.`when`<CampaignCreateRes>(campaignService!!.createCampaign("shelter-001", 100L, req))
            .thenThrow(BusinessException(CampaignErrorCode.UNAUTHORIZED_SHELTER))

        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/api/v1/shelters/shelter-001/campaign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper!!.writeValueAsString(req))
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden())
    }

    // 보호소 캠페인 조회 성공
    @Test
    @Throws(Exception::class)
    fun getCampaign_success() {
        val res = CampaignDetailRes(0, mutableListOf<CampaignDetailItem>())
        Mockito.`when`<CampaignDetailRes>(campaignService!!.getCampaign("shelter-001")).thenReturn(res)

        mockMvc!!.perform(MockMvcRequestBuilders.get("/api/v1/shelters/shelter-001/campaign"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.campaignCount").value(0))
    }

    // 보호소 캠페인 조회 - 비로그인도 가능
    @Test
    @WithAnonymousUser
    @Throws(Exception::class)
    fun getCampaign_anonymous_success() {
        val res = CampaignDetailRes(0, mutableListOf<CampaignDetailItem>())
        Mockito.`when`<CampaignDetailRes>(campaignService!!.getCampaign("shelter-001")).thenReturn(res)

        mockMvc!!.perform(MockMvcRequestBuilders.get("/api/v1/shelters/shelter-001/campaign"))
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    // 캠페인 종료 성공
    @Test
    @Throws(Exception::class)
    fun closeCampaign_success() {
        Mockito.doNothing().`when`<CampaignService?>(campaignService).closeCampaign(100L, 1L)

        mockMvc!!.perform(MockMvcRequestBuilders.patch("/api/v1/campaigns/1/status"))
            .andExpect(MockMvcResultMatchers.status().isNoContent())
    }

    // 캠페인 종료 실패 - 비로그인 (401)
    @Test
    @WithAnonymousUser
    @Throws(Exception::class)
    fun closeCampaign_fail_unauthorized() {
        mockMvc!!.perform(MockMvcRequestBuilders.patch("/api/v1/campaigns/1/status"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
    }

    // 캠페인 종료 실패 - 이미 마감된 캠페인 (400)
    @Test
    @Throws(Exception::class)
    fun closeCampaign_fail_alreadyClosed() {
        Mockito.doThrow(BusinessException(CampaignErrorCode.CAMPAIGN_CLOSED))
            .`when`<CampaignService?>(campaignService).closeCampaign(100L, 1L)

        mockMvc!!.perform(MockMvcRequestBuilders.patch("/api/v1/campaigns/1/status"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
    }
}