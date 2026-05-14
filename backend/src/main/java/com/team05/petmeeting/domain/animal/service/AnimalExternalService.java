package com.team05.petmeeting.domain.animal.service;

import com.team05.petmeeting.domain.animal.client.AnimalApiClient;
import com.team05.petmeeting.domain.animal.config.AnimalApiProperties;
import com.team05.petmeeting.domain.animal.dto.external.AnimalApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AnimalExternalService {
    private static final DateTimeFormatter API_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final AnimalApiClient animalApiClient;
    private final AnimalApiProperties animalApiProperties;

    public AnimalApiResponse fetchAnimals(int pageNo, int numOfRows) {
        return fetchAnimals(pageNo, numOfRows, null, null);
    }

    public AnimalApiResponse fetchAnimals(int pageNo, int numOfRows, LocalDate bgnde, LocalDate endde) {
        UriComponentsBuilder builder = createBaseAnimalUri(pageNo, numOfRows);

        if (bgnde != null) {
            builder.queryParam("bgnde", formatDate(bgnde));
        }
        if (endde != null) {
            builder.queryParam("endde", formatDate(endde));
        }

        return fetch(builder.toUriString());
    }

    public AnimalApiResponse fetchAnimalsByUpdatedDate(int pageNo, int numOfRows, LocalDate bgupd, LocalDate enupd) {
        UriComponentsBuilder builder = createBaseAnimalUri(pageNo, numOfRows);

        if (bgupd != null) {
            builder.queryParam("bgupd", formatDate(bgupd));
        }
        if (enupd != null) {
            builder.queryParam("enupd", formatDate(enupd));
        }

        return fetch(builder.toUriString());
    }

    private UriComponentsBuilder createBaseAnimalUri(int pageNo, int numOfRows) {
        return UriComponentsBuilder.fromUriString(animalApiClient.getAbandonmentUrl())
                .queryParam("serviceKey", animalApiClient.getServiceKey())
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .queryParam("_type", animalApiClient.getReturnType());
    }

    private AnimalApiResponse fetch(String url) {
        return createRestClient()
                .get()
                .uri(url)
                .retrieve()
                .body(AnimalApiResponse.class);
    }

    private RestClient createRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(animalApiProperties.getTimeoutMs());
        factory.setReadTimeout(animalApiProperties.getTimeoutMs());

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    private String formatDate(LocalDate date) {
        return date.format(API_DATE_FORMATTER);
    }
}
