package com.team05.petmeeting.domain.adoption.controller;

import com.team05.petmeeting.domain.adoption.dto.request.AdoptionReviewRequest;
import com.team05.petmeeting.domain.adoption.dto.response.AdoptionApplyResponse;
import com.team05.petmeeting.domain.adoption.dto.response.AdoptionDetailResponse;
import com.team05.petmeeting.domain.adoption.service.AdoptionAdminService;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adoptions/admin")
@RequiredArgsConstructor
public class AdoptionAdminController {

    private final AdoptionAdminService adoptionAdminService;

    // 보호소 관리자가 담당 보호소에 접수된 입양 신청 목록을 조회한다.
    @GetMapping("/shelters/{careRegNo}/applications")
    public List<AdoptionApplyResponse> getManagedShelterApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String careRegNo
    ) {
        return adoptionAdminService.getManagedShelterApplications(userDetails.getUserId(), careRegNo);
    }

    // 보호소 관리자가 담당 보호소의 입양 신청 상세 정보를 조회한다.
    @GetMapping("/shelters/{careRegNo}/applications/{applicationId}")
    public AdoptionDetailResponse getManagedShelterApplicationDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String careRegNo,
            @PathVariable Long applicationId
    ) {
        return adoptionAdminService.getManagedShelterApplicationDetail(userDetails.getUserId(), careRegNo, applicationId);
    }

    // 보호소 관리자가 담당 보호소의 입양 신청 상태를 검토한다.
    @PatchMapping("/shelters/{careRegNo}/applications/{applicationId}/review")
    public AdoptionDetailResponse reviewApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String careRegNo,
            @PathVariable Long applicationId,
            @RequestBody AdoptionReviewRequest request
    ) {
        return adoptionAdminService.reviewApplication(userDetails.getUserId(), careRegNo, applicationId, request);
    }
}
