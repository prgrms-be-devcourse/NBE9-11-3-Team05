package com.team05.petmeeting.domain.campaign.controller;

import com.team05.petmeeting.domain.campaign.dto.*;
import com.team05.petmeeting.domain.campaign.service.CampaignService;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CampaignController {
    private final CampaignService campaignService;

    @Operation(summary="현재 진행 캠페인 전체 조회")
    @GetMapping("/campaigns")
    public ResponseEntity<CampaignRes> getCampaigns(){
        CampaignRes res = campaignService.getAllCampaigns();
        return ResponseEntity.ok(res);
    }

    @Operation(summary="보호소 캠페인 생성")
    @PostMapping("/shelters/{shelterId}/campaign")
    public ResponseEntity<CampaignCreateRes> createCampaign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String shelterId,
            @Valid @RequestBody CampaignCreateReq req
            ){
        CampaignCreateRes res = campaignService.createCampaign(shelterId, userDetails.getUserId(), req);
        return ResponseEntity.ok(res);
    }

    @Operation(summary="보호소 현재 진행 캠페인 전체 조회")
    @GetMapping("/shelters/{shelterId}/campaign")
    public ResponseEntity<CampaignDetailRes> getCampaign(
            @PathVariable String shelterId
    ){
        CampaignDetailRes res = campaignService.getCampaign(shelterId);
        return ResponseEntity.ok(res);
    }

    @Operation(summary="캠페인 상태 변경 (진행 중인 캠페인 종료)")
    @PatchMapping("/campaigns/{campaignId}/status")
    public ResponseEntity<Void> closeCampaign(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long campaignId
    ){
        campaignService.closeCampaign(userDetails.getUserId(), campaignId);
        return ResponseEntity.noContent().build();
    }

}
