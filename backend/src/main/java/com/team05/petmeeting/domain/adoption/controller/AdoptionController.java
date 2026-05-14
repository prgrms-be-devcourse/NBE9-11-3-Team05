package com.team05.petmeeting.domain.adoption.controller;

import com.team05.petmeeting.domain.adoption.dto.AdoptionApplyReq;
import com.team05.petmeeting.domain.adoption.dto.AdoptionApplyRes;
import com.team05.petmeeting.domain.adoption.dto.AdoptionDetailRes;
import com.team05.petmeeting.domain.adoption.service.AdoptionService;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adoptions")
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionService adoptionService;

    // 로그인한 사용자의 입양 신청 목록을 조회한다.
    @GetMapping("/me")
    public List<AdoptionApplyRes> getMyAdoptions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        return adoptionService.getMyAdoptions(userId);
    }

    // 로그인한 사용자의 입양 신청 상세를 단건 조회한다.
    @GetMapping("/{applicationId}")
    public AdoptionDetailRes getApplicationDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long applicationId
    ) {
        return adoptionService.getApplicationDetail(userDetails.getUserId(), applicationId);
    }

    // 로그인한 사용자가 특정 동물에 대한 입양 신청서를 제출한다.
    @PostMapping("/{animalId}")
    public AdoptionApplyRes applyApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long animalId,
            @RequestBody AdoptionApplyReq request) {
        return adoptionService.applyApplication(userDetails.getUserId(), animalId, request);
    }

    // 로그인한 사용자가 본인의 입양 신청을 취소한다.
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> cancelApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long applicationId) {
        adoptionService.cancelApplication(userDetails.getUserId(), applicationId);
        return ResponseEntity.noContent().build();
    }
}
