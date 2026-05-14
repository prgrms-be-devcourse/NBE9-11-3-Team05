package com.team05.petmeeting.domain.ads.client;

import com.team05.petmeeting.domain.ads.config.InstagramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class InstagramClient {

    private final InstagramProperties properties;

    private static final String BASE_URL = "https://graph.facebook.com/v19.0";

    // 1단계: 미디어 컨테이너 생성
    public String createMediaContainer(String imageUrl, String caption) {
        String url = BASE_URL + "/" + properties.getUserId() + "/media"
                + "?image_url=" + imageUrl  // URLEncoder 제거
                + "&caption=" + caption
                + "&access_token=" + properties.getAccessToken();

        try {
            return RestClient.create()
                    .post()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "미디어 컨테이너 생성 실패: %s".formatted(e.getResponseBodyAsString()), e
            );
        }
    }

    // 2단계: 미디어 게시
    public String publishMedia(String containerId) {
        String url = BASE_URL + "/" + properties.getUserId() + "/media_publish";

        String body = "creation_id=" + containerId
                + "&access_token=" + properties.getAccessToken();

        try {
            return RestClient.create()
                    .post()
                    .uri(url)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "미디어 게시 실패: %s".formatted(e.getResponseBodyAsString()), e
            );
        }
    }
}