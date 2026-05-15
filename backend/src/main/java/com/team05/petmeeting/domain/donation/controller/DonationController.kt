package com.team05.petmeeting.domain.donation.controller

import com.team05.petmeeting.domain.donation.dto.CompleteReq
import com.team05.petmeeting.domain.donation.dto.CompleteRes
import com.team05.petmeeting.domain.donation.dto.PrepareReq
import com.team05.petmeeting.domain.donation.dto.PrepareRes
import com.team05.petmeeting.domain.donation.service.DonationService
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/donations")
class DonationController(private val donationService: DonationService) {
    @Operation(summary = "결제 준비")
    @PostMapping("/prepare")
    fun prepareDonation(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid req: @Valid PrepareReq
    ): ResponseEntity<PrepareRes?> {
        val res = donationService.prepare(userDetails.getUserId(), req)
        return ResponseEntity.ok<PrepareRes?>(res)
    }

    @Operation(summary = "결제 완료")
    @PostMapping("/complete")
    fun completeDonation(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid req: @Valid CompleteReq
    ): ResponseEntity<CompleteRes?> {
        val res = donationService.donate(userDetails.getUserId(), req)
        return ResponseEntity.ok<CompleteRes?>(res)
    }
}
