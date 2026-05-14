package com.team05.petmeeting.domain.ads.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team05.petmeeting.domain.ads.config.GeminiApiProperties;
import com.team05.petmeeting.domain.ads.dto.GeminiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {

    private final GeminiApiProperties geminiApiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String generate(String prompt) {
        // JSON 직접 문자열 대신 객체로 직렬화 → 특수문자 자동 이스케이프
        GeminiRequest request = GeminiRequest.from(prompt);

        String response;
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            response = RestClient.create()
                    .post()
                    .uri(geminiApiProperties.getUrl() + "?key=" + geminiApiProperties.getKey())
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("Gemini 요청 실패", e);
        }

        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Gemini 파싱 실패", e);
        }
    }
}