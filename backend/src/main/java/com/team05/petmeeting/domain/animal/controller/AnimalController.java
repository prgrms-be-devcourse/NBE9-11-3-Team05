package com.team05.petmeeting.domain.animal.controller;

import com.team05.petmeeting.domain.animal.dto.AnimalRes;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.service.AnimalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/animals")
@Tag(name = "AnimalController", description = "동물 조회 API")
@RequiredArgsConstructor
public class AnimalController {

    private final AnimalService animalService;


    record PageResBody<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {
    }


    @GetMapping()
    @Operation(summary = "유기동물 필터 적용 조회", description = "필터(지역, 축종, 상태)와 페이징/정렬을 지원합니다.")
    public ResponseEntity<PageResBody<AnimalRes>> animalList(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String kind,
            @RequestParam(required = false) Integer stateGroup,
            @PageableDefault(page = 0, size = 12, sort = "noticeEdt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<AnimalRes> page = animalService.getAnimals(region, kind, stateGroup, pageable);

        PageResBody<AnimalRes> response = new PageResBody<>(
                page.getContent(),      // 현재 페이지에 해당하는 List<AnimalRes>
                page.getNumber(),       // 현재 보고 있는 페이지 번호 (0부터 시작)
                page.getSize(),         // 페이지 크기 ex.20
                page.getTotalElements(),// 전체 개수   ex.342
                page.getTotalPages(),   // 전체 페이지 ex.18
                page.isLast()           // 마지막 페이지 여부
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{animalId}")
    @Operation(summary = "유기동물 상세 조회")
    public ResponseEntity<AnimalRes> animalDetail(
            @PathVariable Long animalId
    ) {
        Animal animal = animalService.findByAnimalId(animalId);
        AnimalRes response = new AnimalRes(animal);
        return ResponseEntity.ok(response);
    }


}
