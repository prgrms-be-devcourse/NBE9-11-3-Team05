package com.team05.petmeeting.domain.animal.service

import com.team05.petmeeting.domain.animal.client.AnimalApiClient
import com.team05.petmeeting.domain.animal.config.AnimalApiProperties
import com.team05.petmeeting.domain.animal.dto.external.AnimalApiResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class AnimalExternalService(
    private val animalApiClient: AnimalApiClient,
    private val animalApiProperties: AnimalApiProperties,
) {
    fun fetchAnimals(pageNo: Int, numOfRows: Int): AnimalApiResponse? =
        fetchAnimals(pageNo, numOfRows, null, null)

    fun fetchAnimals(
        pageNo: Int,
        numOfRows: Int,
        bgnde: LocalDate?,
        endde: LocalDate?,
    ): AnimalApiResponse? {
        val builder = createBaseAnimalUri(pageNo, numOfRows)

        if (bgnde != null) {
            builder.queryParam("bgnde", formatDate(bgnde))
        }
        if (endde != null) {
            builder.queryParam("endde", formatDate(endde))
        }

        return fetch(builder.toUriString())
    }

    fun fetchAnimalsByUpdatedDate(
        pageNo: Int,
        numOfRows: Int,
        bgupd: LocalDate?,
        enupd: LocalDate?,
    ): AnimalApiResponse? {
        val builder = createBaseAnimalUri(pageNo, numOfRows)

        if (bgupd != null) {
            builder.queryParam("bgupd", formatDate(bgupd))
        }
        if (enupd != null) {
            builder.queryParam("enupd", formatDate(enupd))
        }

        return fetch(builder.toUriString())
    }

    private fun createBaseAnimalUri(pageNo: Int, numOfRows: Int): UriComponentsBuilder =
        UriComponentsBuilder.fromUriString(animalApiClient.abandonmentUrl)
            .queryParam("serviceKey", animalApiClient.serviceKey)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("_type", animalApiClient.returnType)

    private fun fetch(url: String): AnimalApiResponse? =
        createRestClient()
            .get()
            .uri(url)
            .retrieve()
            .body(AnimalApiResponse::class.java)

    private fun createRestClient(): RestClient {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(animalApiProperties.timeoutMs)
        factory.setReadTimeout(animalApiProperties.timeoutMs)

        return RestClient.builder()
            .requestFactory(factory)
            .build()
    }

    private fun formatDate(date: LocalDate): String = date.format(API_DATE_FORMATTER)

    companion object {
        private val API_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE
    }
}
