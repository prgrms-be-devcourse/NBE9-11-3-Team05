package com.team05.petmeeting.domain.campaign.controller

import com.team05.petmeeting.domain.campaign.dto.CampaignCreateReq
import com.team05.petmeeting.domain.campaign.dto.CampaignCreateRes
import com.team05.petmeeting.domain.campaign.dto.CampaignDetailRes
import com.team05.petmeeting.domain.campaign.dto.CampaignRes
import com.team05.petmeeting.domain.campaign.service.CampaignService
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class CampaignController(private val campaignService: CampaignService) {
    @get:GetMapping("/campaigns")
    @get:Operation(summary = "현재 진행 캠페인 전체 조회")
    val campaigns: ResponseEntity<CampaignRes?>
        get() {
            val res = campaignService.allCampaigns
            return ResponseEntity.ok<CampaignRes?>(res)
        }

    @Operation(summary = "보호소 캠페인 생성")
    @PostMapping("/shelters/{shelterId}/campaign")
    fun createCampaign(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable shelterId: String,
        @Valid @RequestBody req: @Valid CampaignCreateReq
    ): ResponseEntity<CampaignCreateRes?> {
        val res = campaignService.createCampaign(shelterId, userDetails.getUserId(), req)
        return ResponseEntity.ok<CampaignCreateRes?>(res)
    }

    @Operation(summary = "보호소 현재 진행 캠페인 전체 조회")
    @GetMapping("/shelters/{shelterId}/campaign")
    fun getCampaign(
        @PathVariable shelterId: String
    ): ResponseEntity<CampaignDetailRes?> {
        val res = campaignService.getCampaign(shelterId)
        return ResponseEntity.ok<CampaignDetailRes?>(res)
    }

    @Operation(summary = "캠페인 상태 변경 (진행 중인 캠페인 종료)")
    @PatchMapping("/campaigns/{campaignId}/status")
    fun closeCampaign(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable campaignId: Long
    ): ResponseEntity<Void?> {
        campaignService.closeCampaign(userDetails.getUserId(), campaignId)
        return ResponseEntity.noContent().build<Void?>()
    }
}
