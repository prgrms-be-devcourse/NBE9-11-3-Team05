package com.team05.petmeeting.domain.ads.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.team05.petmeeting.domain.ads.client.InstagramClient
import com.team05.petmeeting.domain.animal.entity.Animal
import com.team05.petmeeting.domain.animal.repository.AnimalRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AdsService(
    private val animalRepository: AnimalRepository,
    private val cardNewsService: CardNewsService,
    private val instagramClient: InstagramClient,
    private val objectMapper: ObjectMapper
) {
    // Top N 동물 조회 (보호중인 동물만)
    fun getTopAnimals(n: Int): List<Animal> {
        return animalRepository.findAllByStateGroupOrderByTotalCheerCountDesc(
            0,  // 0 = 보호중
            PageRequest.of(0, n)
        )
    }

    // 전체 파이프라인 실행
    @Throws(InterruptedException::class)
    fun runWeeklyAds(n: Int) {
        val topAnimals = getTopAnimals(n)

        for (animal in topAnimals) {
            // 1. 카드뉴스 생성
            val cardNews = cardNewsService.generateCardNews(animal)

            // 2. 컨테이너 생성
            val containerResponse = instagramClient.createMediaContainer(
                cardNews.imageUrl,
                cardNews.caption
            ) ?: throw IllegalStateException("인스타그램 미디어 컨테이너 응답이 비어있습니다.")
            val containerId = extractId(containerResponse)

            // 3. 인스타그램 이미지 처리 대기 (5초)
            Thread.sleep(5000)

            // 4. 게시
            instagramClient.publishMedia(containerId)
        }
    }

    private fun extractId(response: String): String {
        return try {
            objectMapper.readTree(response)
                .path("id")
                .asText(null)
                ?: throw IllegalStateException("인스타그램 응답에서 id를 찾을 수 없습니다.")
        } catch (e: Exception) {
            throw IllegalStateException("인스타그램 미디어 컨테이너 ID 추출 실패", e)
        }
    }

    // @Scheduled(cron = "0 0 9 * * MON") // 매주 월요일 오전 9시 스프링이 직접 자동 실행하는 어노테이션, 일단 주석처리
    @Throws(InterruptedException::class)
    fun scheduledWeeklyAds() {
        runWeeklyAds(3)
    }
}
