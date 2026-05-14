package com.team05.petmeeting.domain.animal;

import com.team05.petmeeting.domain.animal.client.AnimalApiClient;
import com.team05.petmeeting.domain.animal.config.AnimalApiProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// AnimalSyncController 동기화 흐름에서 외부 동물 API URL과 인증 설정을 제공하는 AnimalApiClient 검증
public class AnimalApiClientTest {
    private static final String BASE_URL = "https://apis.data.go.kr/1543061/abandonmentPublicService_v2";

    private AnimalApiClient createAnimalApiClient() {
        AnimalApiProperties properties = new AnimalApiProperties();
        properties.setBaseUrl(BASE_URL);
        properties.setServiceKey("test-key");
        properties.setReturnType("json");
        return new AnimalApiClient(properties);
    }

    @Test
    @DisplayName("유기동물 목록 조회 URL 생성")
    void t1() {
        // given
        AnimalApiClient animalApiClient = createAnimalApiClient();

        // when
        String result = animalApiClient.getAbandonmentUrl();

        // then
        assertEquals(
                BASE_URL + "/abandonmentPublic_v2",
                result
        );
    }

    @Test
    @DisplayName("시도 조회 URL 생성")
    void t2() {
        // given
        AnimalApiClient animalApiClient = createAnimalApiClient();

        // when
        String result = animalApiClient.getSidoUrl();

        // then
        assertEquals(
                BASE_URL + "/sido_v2",
                result
        );

    }

    @Test
    @DisplayName("시군구 조회 URL 생성")
    void t3() {
        // given
        AnimalApiClient animalApiClient = createAnimalApiClient();

        // when
        String result = animalApiClient.getSigunguUrl();

        // then
        assertEquals(
                BASE_URL + "/sigungu_v2",
                result
        );
    }

    @Test
    @DisplayName("보호소 조회 URL 생성")
    void shelterUrl() {
        // given
        AnimalApiClient animalApiClient = createAnimalApiClient();

        // when
        String result = animalApiClient.getShelterUrl();

        // then
        assertEquals(
                BASE_URL + "/shelter_v2",
                result
        );
    }

    @Test
    @DisplayName("품종 조회 URL을 생성")
    void kindUrl() {
        // given
        AnimalApiClient animalApiClient = createAnimalApiClient();

        // when
        String result = animalApiClient.getKindUrl();

        // then
        assertEquals(
                BASE_URL + "/kind_v2",
                result
        );
    }

    @Test
    @DisplayName("서비스키 반환")
    void getServiceKey() {
        // given
        AnimalApiClient animalApiClient = createAnimalApiClient();

        // when
        String result = animalApiClient.getServiceKey();

        // then
        assertEquals("test-key", result);
    }

    @Test
    @DisplayName("응답 타입 반환")
    void getReturnType() {
        // given
        AnimalApiClient animalApiClient = createAnimalApiClient();

        // when
        String result = animalApiClient.getReturnType();

        // then
        assertEquals("json", result);
    }

}
