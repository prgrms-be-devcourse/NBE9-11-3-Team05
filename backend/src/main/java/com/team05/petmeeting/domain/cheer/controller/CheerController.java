package com.team05.petmeeting.domain.cheer.controller;

import com.team05.petmeeting.domain.cheer.dto.CheerRes;
import com.team05.petmeeting.domain.cheer.dto.CheerStatusDto;
import com.team05.petmeeting.domain.cheer.service.CheerService;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "CheerController", description = "응원 API")
@RequiredArgsConstructor
public class CheerController {

    private final CheerService cheerService;

    @GetMapping("/cheers/today")
    @Operation(summary = "잔여 응원 횟수 조회")
    public ResponseEntity<CheerStatusDto> getTodaysCheers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Service 호출
        CheerStatusDto status = cheerService.getTodaysStatus(userDetails.getUserId());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/animals/{animalId}/cheers")
    @Operation(summary = "응원 부여")
    public ResponseEntity<CheerRes> cheerAnimal(
            @PathVariable Long animalId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CheerRes cheerRes = cheerService.cheerAnimal(userDetails.getUserId(), animalId);

        return ResponseEntity.ok(cheerRes);
    }


}
