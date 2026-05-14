package com.team05.petmeeting.domain.donation.controller;

import com.team05.petmeeting.domain.donation.dto.CompleteReq;
import com.team05.petmeeting.domain.donation.dto.CompleteRes;
import com.team05.petmeeting.domain.donation.dto.PrepareReq;
import com.team05.petmeeting.domain.donation.dto.PrepareRes;
import com.team05.petmeeting.domain.donation.service.DonationService;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/donations")
public class DonationController {

    private final DonationService donationService;

    @Operation(summary="결제 준비")
    @PostMapping("/prepare")
    public ResponseEntity<PrepareRes> prepareDonation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid PrepareReq req
            ){
        PrepareRes res = donationService.prepare(userDetails.getUserId(), req);
        return ResponseEntity.ok(res);
    }

    @Operation(summary="결제 완료")
    @PostMapping("/complete")
    public ResponseEntity<CompleteRes> completeDonation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid CompleteReq req
    ){
        CompleteRes res = donationService.donate(userDetails.getUserId(), req);
        return ResponseEntity.ok(res);
    }
}
