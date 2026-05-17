package com.team05.petmeeting.domain.ads.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.team05.petmeeting.domain.ads.client.InstagramClient
import com.team05.petmeeting.domain.ads.dto.CardNewsResult
import com.team05.petmeeting.domain.animal.entity.Animal
import com.team05.petmeeting.domain.animal.repository.AnimalRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Pageable

@ExtendWith(MockitoExtension::class)
internal class AdsServiceTest {
    @Mock
    private lateinit var animalRepository: AnimalRepository

    @Mock
    private lateinit var cardNewsService: CardNewsService

    @Mock
    private lateinit var instagramClient: InstagramClient

    private lateinit var adsService: AdsService

    @BeforeEach
    fun setUp() {
        adsService = AdsService(
            animalRepository,
            cardNewsService,
            instagramClient,
            ObjectMapper()
        )
    }

    @Test
    @DisplayName("Top N 동물 조회 테스트")
    fun getTopAnimals() {
        val animal = Animal()
        Mockito.`when`(
            animalRepository.findAllByStateGroupOrderByTotalCheerCountDesc(
                eq(0),
                anyPageable()
            )
        ).thenReturn(listOf(animal))

        val result = adsService.getTopAnimals(3)

        assertThat(result).hasSize(1)
        assertThat(result[0]).isSameAs(animal)
    }

    @Test
    @DisplayName("인스타그램 컨테이너 응답에서 ID를 JSON으로 추출한다")
    fun runWeeklyAdsExtractsContainerIdFromJson() {
        val animal = Animal()

        Mockito.`when`(
            animalRepository.findAllByStateGroupOrderByTotalCheerCountDesc(
                eq(0),
                anyPageable()
            )
        ).thenReturn(listOf(animal))
        Mockito.`when`(cardNewsService.generateCardNews(animal))
            .thenReturn(CardNewsResult("https://image-url.com/card.png", "caption"))
        Mockito.`when`(
            instagramClient.createMediaContainer(
                eq("https://image-url.com/card.png"),
                eq("caption")
            )
        ).thenReturn("{ \"id\" : \"container-123\" }")

        adsService.runWeeklyAds(1)

        Mockito.verify(instagramClient).publishMedia("container-123")
    }

    private fun anyPageable(): Pageable {
        return any(Pageable::class.java) ?: Pageable.unpaged()
    }
}
