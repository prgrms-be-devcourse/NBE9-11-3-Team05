package com.team05.petmeeting.domain.ads.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.team05.petmeeting.domain.ads.config.GeminiApiProperties
import com.team05.petmeeting.domain.ads.dto.GeminiRequest.Companion.from
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class GeminiServiceImpl(
    private val geminiApiProperties: GeminiApiProperties,
    private val objectMapper: ObjectMapper
) : GeminiService {
    override fun generate(prompt: String): String {
        // JSON 직접 문자열 대신 객체로 직렬화 → 특수문자 자동 이스케이프
        val request = from(prompt)

        val response: String?
        try {
            val requestBody = objectMapper.writeValueAsString(request)
            response = RestClient.create()
                .post()
                .uri(geminiApiProperties.url + "?key=" + geminiApiProperties.key)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Gemini 요청 실패", e)
        }

        try {
            val root = objectMapper.readTree(response)
            return root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText(null)
                ?: throw RuntimeException("Gemini 응답에서 text를 찾을 수 없습니다.")
        } catch (e: Exception) {
            throw RuntimeException("Gemini 파싱 실패", e)
        }
    }
}
