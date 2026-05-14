package com.team05.petmeeting.domain.animal;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.team05.petmeeting.domain.animal.client.AnimalApiClient;
import com.team05.petmeeting.domain.animal.config.AnimalApiProperties;
import com.team05.petmeeting.domain.animal.dto.external.AnimalApiResponse;
import com.team05.petmeeting.domain.animal.service.AnimalExternalService;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// AnimalSyncController 동기화 흐름에서 외부 동물 API 호출 파라미터와 응답 매핑을 담당하는 AnimalExternalService 검증
class AnimalExternalServiceTest {

    private HttpServer server;
    private AtomicReference<String> capturedQuery;
    private AnimalExternalService animalExternalService;

    @BeforeEach
    void setUp() throws IOException {
        capturedQuery = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/abandonmentPublic_v2", this::handleAnimalRequest);
        server.start();

        AnimalApiProperties properties = new AnimalApiProperties();
        properties.setBaseUrl("http://localhost:" + server.getAddress().getPort());
        properties.setServiceKey("test-key");
        properties.setReturnType("json");
        properties.setTimeoutMs(1000);

        AnimalApiClient animalApiClient = new AnimalApiClient(properties);
        animalExternalService = new AnimalExternalService(animalApiClient, properties);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("일반 동물 조회 요청 파라미터와 응답 매핑")
    void fetchAnimals() {
        // when
        AnimalApiResponse response = animalExternalService.fetchAnimals(
                2,
                30,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        );

        // then
        Map<String, String> queryParams = parseQuery(capturedQuery.get());
        assertThat(queryParams)
                .containsEntry("serviceKey", "test-key")
                .containsEntry("pageNo", "2")
                .containsEntry("numOfRows", "30")
                .containsEntry("_type", "json")
                .containsEntry("bgnde", "20260401")
                .containsEntry("endde", "20260430");

        assertThat(response.getResponse().getBody().getItems().getItem())
                .hasSize(1)
                .first()
                .satisfies(item -> {
                    assertThat(item.getDesertionNo()).isEqualTo("D-001");
                    assertThat(item.getProcessState()).isEqualTo("보호중");
                    assertThat(item.getCareRegNo()).isEqualTo("CARE-001");
                });
    }

    @Test
    @DisplayName("수정일 기준 동물 조회 요청 파라미터")
    void fetchAnimalsByUpdatedDate() {
        // when
        AnimalApiResponse response = animalExternalService.fetchAnimalsByUpdatedDate(
                1,
                50,
                LocalDate.of(2026, 4, 20),
                LocalDate.of(2026, 4, 23)
        );

        // then
        Map<String, String> queryParams = parseQuery(capturedQuery.get());
        assertThat(queryParams)
                .containsEntry("serviceKey", "test-key")
                .containsEntry("pageNo", "1")
                .containsEntry("numOfRows", "50")
                .containsEntry("_type", "json")
                .containsEntry("bgupd", "20260420")
                .containsEntry("enupd", "20260423");
        assertThat(response.getResponse().getBody().getItems().getItem()).hasSize(1);
    }

    private void handleAnimalRequest(HttpExchange exchange) throws IOException {
        capturedQuery.set(exchange.getRequestURI().getRawQuery());

        byte[] responseBody = animalApiResponseJson().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBody.length);
        try (OutputStream responseStream = exchange.getResponseBody()) {
            responseStream.write(responseBody);
        }
    }

    private String animalApiResponseJson() {
        return """
                {
                  "response": {
                    "header": {
                      "reqNo": 1,
                      "resultCode": "00",
                      "resultMsg": "NORMAL SERVICE"
                    },
                    "body": {
                      "items": {
                        "item": [
                          {
                            "desertionNo": "D-001",
                            "processState": "보호중",
                            "noticeNo": "NOTICE-001",
                            "noticeEdt": "20260430",
                            "happenPlace": "테스트 장소",
                            "upKindNm": "개",
                            "kindFullNm": "믹스견",
                            "colorCd": "흰색",
                            "age": "2024(년생)",
                            "weight": "5(Kg)",
                            "sexCd": "M",
                            "popfile1": "https://example.com/image1.jpg",
                            "popfile2": "https://example.com/image2.jpg",
                            "specialMark": "온순함",
                            "careOwnerNm": "담당자",
                            "careNm": "테스트 보호소",
                            "careAddr": "테스트 주소",
                            "careTel": "010-1234-5678",
                            "updTm": "2026-04-21 10:00:00.0",
                            "careRegNo": "CARE-001",
                            "orgNm": "테스트 기관"
                          }
                        ]
                      },
                      "numOfRows": 1,
                      "pageNo": 1,
                      "totalCount": 1
                    }
                  }
                }
                """;
    }

    private Map<String, String> parseQuery(String rawQuery) {
        return Arrays.stream(rawQuery.split("&"))
                .map(param -> param.split("=", 2))
                .collect(Collectors.toMap(
                        parts -> parts[0],
                        parts -> parts.length > 1 ? parts[1] : ""
                ));
    }
}
