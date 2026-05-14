package com.team05.petmeeting.domain.campaign.controller;

import com.team05.petmeeting.domain.campaign.dto.CampaignCreateReq;
import com.team05.petmeeting.domain.campaign.dto.CampaignCreateRes;
import com.team05.petmeeting.domain.campaign.dto.CampaignDetailRes;
import com.team05.petmeeting.domain.campaign.dto.CampaignRes;
import com.team05.petmeeting.domain.campaign.enums.CampaignStatus;
import com.team05.petmeeting.domain.campaign.errorCode.CampaignErrorCode;
import com.team05.petmeeting.domain.campaign.service.CampaignService;
import com.team05.petmeeting.global.exception.BusinessException;
import com.team05.petmeeting.global.security.test.WithCustomUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithCustomUser(userId = 100L)
class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampaignService campaignService;

    @Autowired
    private ObjectMapper objectMapper;

    // 전체 캠페인 조회 성공
    @Test
    void getCampaigns_success() throws Exception {
        CampaignRes res = new CampaignRes(0, List.of());
        when(campaignService.getAllCampaigns()).thenReturn(res);

        mockMvc.perform(get("/api/v1/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCampaigns").value(0));
    }

    // 전체 캠페인 조회 - 비로그인도 가능
    @Test
    @WithAnonymousUser
    void getCampaigns_anonymous_success() throws Exception {
        CampaignRes res = new CampaignRes(0, List.of());
        when(campaignService.getAllCampaigns()).thenReturn(res);

        mockMvc.perform(get("/api/v1/campaigns"))
                .andExpect(status().isOk());
    }

    // 보호소 캠페인 생성 성공
    @Test
    void createCampaign_success() throws Exception {
        CampaignCreateReq req = new CampaignCreateReq("테스트 캠페인", "설명", 100000);
        CampaignCreateRes res = new CampaignCreateRes(1L, "테스트 캠페인", 100000, CampaignStatus.ACTIVE);
        when(campaignService.createCampaign("shelter-001", 100L, req)).thenReturn(res);

        mockMvc.perform(post("/api/v1/shelters/shelter-001/campaign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 캠페인"))
                .andExpect(jsonPath("$.targetAmount").value(100000));
    }

    // 캠페인 생성 실패 - 비로그인 (401)
    @Test
    @WithAnonymousUser
    void createCampaign_fail_unauthorized() throws Exception {
        CampaignCreateReq req = new CampaignCreateReq("테스트 캠페인", "설명", 100000);

        mockMvc.perform(post("/api/v1/shelters/shelter-001/campaign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // 캠페인 생성 실패 - 권한 없는 유저 (403)
    @Test
    void createCampaign_fail_forbidden() throws Exception {
        CampaignCreateReq req = new CampaignCreateReq("테스트 캠페인", "설명", 100000);
        when(campaignService.createCampaign("shelter-001", 100L, req))
                .thenThrow(new BusinessException(CampaignErrorCode.UNAUTHORIZED_SHELTER));

        mockMvc.perform(post("/api/v1/shelters/shelter-001/campaign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // 보호소 캠페인 조회 성공
    @Test
    void getCampaign_success() throws Exception {
        CampaignDetailRes res = new CampaignDetailRes(0, List.of());
        when(campaignService.getCampaign("shelter-001")).thenReturn(res);

        mockMvc.perform(get("/api/v1/shelters/shelter-001/campaign"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignCount").value(0));
    }

    // 보호소 캠페인 조회 - 비로그인도 가능
    @Test
    @WithAnonymousUser
    void getCampaign_anonymous_success() throws Exception {
        CampaignDetailRes res = new CampaignDetailRes(0, List.of());
        when(campaignService.getCampaign("shelter-001")).thenReturn(res);

        mockMvc.perform(get("/api/v1/shelters/shelter-001/campaign"))
                .andExpect(status().isOk());
    }

    // 캠페인 종료 성공
    @Test
    void closeCampaign_success() throws Exception {
        doNothing().when(campaignService).closeCampaign(100L, 1L);

        mockMvc.perform(patch("/api/v1/campaigns/1/status"))
                .andExpect(status().isNoContent());
    }

    // 캠페인 종료 실패 - 비로그인 (401)
    @Test
    @WithAnonymousUser
    void closeCampaign_fail_unauthorized() throws Exception {
        mockMvc.perform(patch("/api/v1/campaigns/1/status"))
                .andExpect(status().isUnauthorized());
    }

    // 캠페인 종료 실패 - 이미 마감된 캠페인 (400)
    @Test
    void closeCampaign_fail_alreadyClosed() throws Exception {
        doThrow(new BusinessException(CampaignErrorCode.CAMPAIGN_CLOSED))
                .when(campaignService).closeCampaign(100L, 1L);

        mockMvc.perform(patch("/api/v1/campaigns/1/status"))
                .andExpect(status().isBadRequest());
    }
}