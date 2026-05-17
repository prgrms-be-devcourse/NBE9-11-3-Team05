package com.team05.petmeeting.domain.animal.controller

import com.team05.petmeeting.domain.animal.dto.AnimalRes
import com.team05.petmeeting.domain.animal.service.AnimalService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/animals")
@Tag(name = "AnimalController", description = "동물 조회 API")
class AnimalController(
    private val animalService: AnimalService
) {

    data class PageResBody<T>(
        val content: List<T>,
        val page: Int,
        val size: Int,
        val totalElements: Long,
        val totalPages: Int,
        val last: Boolean
    )


    @GetMapping
    @Operation(summary = "유기동물 필터 적용 조회", description = "필터(지역, 축종, 상태)와 페이징/정렬을 지원합니다.")
    fun animalList(
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) kind: String?,
        @RequestParam(required = false) stateGroup: Int?,
        @PageableDefault(page = 0, size = 12, sort = ["noticeEdt"], direction = Sort.Direction.ASC) pageable: Pageable
    ): ResponseEntity<PageResBody<AnimalRes>> {
        val page: Page<AnimalRes> = animalService.getAnimals(region, kind, stateGroup, pageable)

        val response = PageResBody(
            content = page.content,  // 현재 페이지에 해당하는 List<AnimalRes>
            page = page.number,  // 현재 보고 있는 페이지 번호 (0부터 시작)
            size = page.size,  // 페이지 크기 ex.20
            totalElements = page.totalElements,  // 전체 개수   ex.342
            totalPages = page.totalPages,  // 전체 페이지 ex.18
            last = page.isLast // 마지막 페이지 여부
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{animalId}")
    @Operation(summary = "유기동물 상세 조회")
    fun animalDetail(
        @PathVariable animalId: Long
    ): ResponseEntity<AnimalRes> {
        val animal = animalService.findByAnimalId(animalId)
        val response = AnimalRes(animal)
        return ResponseEntity.ok(response)
    }
}
