package com.team05.petmeeting.domain.naming.controller;

import com.team05.petmeeting.domain.naming.dto.*;
import com.team05.petmeeting.domain.naming.repository.AnimalNameCandidateRepository;
import com.team05.petmeeting.domain.naming.service.NamingService;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/naming")
@Tag(name = "NamingController", description = "동물이름 투표(작명) API")
public class NamingController { // 제안/투표/후보조회 API

    private final AnimalNameCandidateRepository candidateRepository;
    private final NamingService namingService;

    @GetMapping("/animals/{animalId}/candidates")
    @Operation(summary = "이름 후보 조회", description = "득표순 내림차순 조회, 동표는 생성일순")
    public ResponseEntity<NameCandidateRes> getNameCandidates(
            @PathVariable Long animalId,
            // 로그인 안 한 유저도 조회는 가능하므로 null 체크 필요
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        NameCandidateRes response = namingService.getCandidates(animalId, userId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/admin/qualified-candidates") // animalId 제거!
    @Operation(summary = "관리자용 확정 대기 전체 목록", description = "우리 보호소 동물 중 10표 이상 얻은 후보가 있는 모든 동물 조회")
    public ResponseEntity<List<NameCandidateRes>> getAdminQualifiedCandidates(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 이제 새로고침해도 유저 정보(토큰)만 있으면 우리 보호소 애들 싹 다 가져옴
        List<NameCandidateRes> response = namingService.getAdminQualifiedList(userDetails.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/animals/{animalId}/propose")
    @Operation(summary = "이름 작명(제안)", description = "새로운 이름을 제안합니다. 이미 존재하면 자동으로 투표 처리됩니다.")
    public ResponseEntity<NameProposalRes> proposeName(
            @PathVariable Long animalId,
            @Valid @RequestBody NameProposalReq request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        NameProposalRes response = namingService.proposeName(animalId, userDetails.getUserId(), request.getProposedName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/candidates/{candidateId}/vote")
    @Operation(summary = "기존 이름 투표")
    public ResponseEntity<Void> voteName(
            @PathVariable Long candidateId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        namingService.vote(candidateId, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/candidates/{candidateId}/confirm")
    @Operation(summary = "관리자가 이름 확정", description = "해당 동물의 보호소 관리자가 최종 확정 실시")
    public ResponseEntity<Void> confirmNameByAdmin(
            @PathVariable Long candidateId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        namingService.confirmName(candidateId, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/badwords")
    @Operation(summary = "금칙어 조회")
    public ResponseEntity<BadWordListRes> getBadWords() {
        BadWordListRes response = namingService.getBadWords();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/badwords")
    @Operation(summary = "금칙어 추가")
    public ResponseEntity<BadWordAddRes> addBadWord(
            @Valid @RequestBody BadWordAddReq request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 관리자 권한 검증 로직은 Security나 Service에서 수행
        BadWordAddRes response = namingService.addBadWord(request.getBadWord());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/badwords/{badwordId}")
    @Operation(summary = "금칙어 삭제")
    public ResponseEntity<Void> deleteBadWord(
            @PathVariable Long badwordId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        namingService.deleteBadWord(badwordId);
        return ResponseEntity.noContent().build(); // 삭제 성공 시 204 No Content
    }


}
