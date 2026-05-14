package com.team05.petmeeting.domain.animal.client;

import com.team05.petmeeting.domain.animal.config.AnimalApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnimalApiClient {

    private final AnimalApiProperties properties; // API 호출에 필요한 설정을 담은 클래스

    private static final String ABANDONMENT_PATH = "/abandonmentPublic_v2";
    private static final String SIDO_PATH = "/sido_v2";
    private static final String SIGUNGU_PATH = "/sigungu_v2";
    private static final String SHELTER_PATH = "/shelter_v2";
    private static final String KIND_PATH = "/kind_v2";

    // API 엔드포인트 URL을 반환하는 메서드들

    // 유기동물 정보 조회 API URL 반환
    public String getAbandonmentUrl() {
        return properties.getBaseUrl() + ABANDONMENT_PATH;
    }

    // 시도 정보 조회 API URL 반환
    public String getSidoUrl() {
        return properties.getBaseUrl() + SIDO_PATH;
    }

    // 시군구 정보 조회 API URL 반환
    public String getSigunguUrl() {
        return properties.getBaseUrl() + SIGUNGU_PATH;
    }

    // 보호소 정보 조회 API URL 반환
    public String getShelterUrl() {
        return properties.getBaseUrl() + SHELTER_PATH;
    }

    // 품종 정보 조회 API URL 반환
    public String getKindUrl() {
        return properties.getBaseUrl() + KIND_PATH;
    }

    // API 인증에 필요한 서비스 키 반환
    public String getServiceKey() {
        return properties.getServiceKey();
    }

    // API 응답 형식 반환
    public String getReturnType() {
        return properties.getReturnType();
    }

}
