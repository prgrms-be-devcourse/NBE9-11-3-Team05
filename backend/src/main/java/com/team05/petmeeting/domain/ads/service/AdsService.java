package com.team05.petmeeting.domain.ads.service;

import com.team05.petmeeting.domain.ads.client.InstagramClient;
import com.team05.petmeeting.domain.ads.dto.CardNewsResult;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdsService {

    private final AnimalRepository animalRepository;
    private final CardNewsService cardNewsService;
    private final InstagramClient instagramClient;

    // Top N 동물 조회 (보호중인 동물만)
    public List<Animal> getTopAnimals(int n) {
        return animalRepository.findAllByStateGroupOrderByTotalCheerCountDesc(
                0,  // 0 = 보호중
                PageRequest.of(0, n)
        );
    }

    // 전체 파이프라인 실행
    public void runWeeklyAds(int n) throws InterruptedException {
        List<Animal> topAnimals = getTopAnimals(n);

        for (Animal animal : topAnimals) {
            // 1. 카드뉴스 생성
            CardNewsResult cardNews = cardNewsService.generateCardNews(animal);

            // 2. 컨테이너 생성
            String containerResponse = instagramClient.createMediaContainer(
                    cardNews.imageUrl,
                    cardNews.caption
            );
            String containerId = extractId(containerResponse);

            // 3. 인스타그램 이미지 처리 대기 (5초)
            Thread.sleep(5000);

            // 4. 게시
            instagramClient.publishMedia(containerId);
        }
    }

    private String extractId(String response) {
        return response.replace("{\"id\":\"", "")
                .replace("\"}", "")
                .trim();
    }

    // @Scheduled(cron = "0 0 9 * * MON") // 매주 월요일 오전 9시 스프링이 직접 자동 실행하는 어노테이션, 일단 주석처리
    public void scheduledWeeklyAds() throws InterruptedException {
        runWeeklyAds(3);
    }
}