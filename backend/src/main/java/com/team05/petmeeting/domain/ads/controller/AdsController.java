package com.team05.petmeeting.domain.ads.controller;

import com.team05.petmeeting.domain.ads.service.AdsService;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ads")
public class AdsController {

    private final AdsService adsService;

    // Top N 동물 조회
    @GetMapping("/top-animals")
    public RsData<List<Animal>> getTopAnimals(
            @RequestParam(defaultValue = "3") int n
    ) {
        List<Animal> topAnimals = adsService.getTopAnimals(n);
        return new RsData<>("Top N 동물 조회 성공", "200", topAnimals);
    }

    // 수동으로 광고 파이프라인 실행
    @PostMapping("/run")
    public RsData<String> runAds(
            @RequestParam(defaultValue = "3") int n
    ) throws InterruptedException {
        adsService.runWeeklyAds(n);
        return new RsData<>("광고 실행 성공", "200", n + "개 동물 인스타그램 업로드 완료");
    }
}