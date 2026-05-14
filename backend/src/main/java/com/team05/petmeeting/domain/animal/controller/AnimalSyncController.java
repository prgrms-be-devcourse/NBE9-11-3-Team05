package com.team05.petmeeting.domain.animal.controller;

import com.team05.petmeeting.domain.animal.dto.AnimalSyncResponse;
import com.team05.petmeeting.domain.animal.service.AnimalSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/animals")
@RequiredArgsConstructor
public class AnimalSyncController {
    private final AnimalSyncService animalSyncService;

    // 특정 페이지 번호와 페이지당 항목 수를 기준으로 유기동물 데이터를 조회하고 DB에 저장하는 엔드포인트
    //1. 실사용 거의 안함 2. 초기 동기화 시에만 사용
    @PostMapping("/sync")
    public ResponseEntity<AnimalSyncResponse> syncAnimals(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows
    ) {
        return ResponseEntity.ok(animalSyncService.fetchAndSaveAnimals(pageNo, numOfRows));
    }

    //최초 적재를 위한거
    @PostMapping("/sync/initial")
    public ResponseEntity<AnimalSyncResponse> syncMonthlyFrom2008(
            @RequestParam(defaultValue = "500") int numOfRows
    ) {
        return ResponseEntity.ok(animalSyncService.runInitialMonthlySync(numOfRows));
    }

    // 마지막 성공 동기화 시각 이후 수정된 데이터만 반영
    @PostMapping("/sync/update")
    public ResponseEntity<AnimalSyncResponse> syncByUpdatedDate(
            @RequestParam(defaultValue = "500") int numOfRows
    ) {
        return ResponseEntity.ok(animalSyncService.runUpdateSync(numOfRows));
    }
}
