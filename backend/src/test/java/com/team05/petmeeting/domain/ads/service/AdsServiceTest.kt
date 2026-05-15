package com.team05.petmeeting.domain.ads.service

import com.team05.petmeeting.domain.ads.client.InstagramClient
import com.team05.petmeeting.domain.animal.entity.Animal
import com.team05.petmeeting.domain.animal.repository.AnimalRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
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

    @InjectMocks
    private lateinit var adsService: AdsService

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

    private fun anyPageable(): Pageable {
        return any(Pageable::class.java) ?: Pageable.unpaged()
    }
}
