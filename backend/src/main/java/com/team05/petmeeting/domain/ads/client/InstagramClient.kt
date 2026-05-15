package com.team05.petmeeting.domain.ads.client

import com.team05.petmeeting.domain.ads.config.InstagramProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class InstagramClient(
    private val properties: InstagramProperties
) {
    // 1단계: 미디어 컨테이너 생성
    fun createMediaContainer(imageUrl: String?, caption: String?): String? {
        val url = (BASE_URL + "/" + properties.userId + "/media"
                + "?image_url=" + imageUrl // URLEncoder 제거
                + "&caption=" + caption
                + "&access_token=" + properties.accessToken)

        try {
            return RestClient.create()
                .post()
                .uri(url)
                .retrieve()
                .body(String::class.java)
        } catch (e: RestClientResponseException) {
            throw IllegalStateException(
                "미디어 컨테이너 생성 실패: ${e.responseBodyAsString}", e
            )
        }
    }

    // 2단계: 미디어 게시
    fun publishMedia(containerId: String): String? {
        val url = BASE_URL + "/" + properties.userId + "/media_publish"

        val body = ("creation_id=" + containerId
                + "&access_token=" + properties.accessToken)

        try {
            return RestClient.create()
                .post()
                .uri(url)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(body)
                .retrieve()
                .body(String::class.java)
        } catch (e: RestClientResponseException) {
            throw IllegalStateException(
                "미디어 게시 실패: ${e.responseBodyAsString}", e
            )
        }
    }

    companion object {
        private const val BASE_URL = "https://graph.facebook.com/v19.0"
    }
}
