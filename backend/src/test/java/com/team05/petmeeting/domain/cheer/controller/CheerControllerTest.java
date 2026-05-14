package com.team05.petmeeting.domain.cheer.controller;


import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode;
import com.team05.petmeeting.domain.cheer.dto.CheerRes;
import com.team05.petmeeting.domain.cheer.dto.CheerStatusDto;
import com.team05.petmeeting.domain.cheer.errorCode.CheerErrorCode;
import com.team05.petmeeting.domain.cheer.service.CheerService;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.global.exception.BusinessException;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class CheerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheerService cheerService;

    private final Long userId = 1L; // 필드로 선언하면 헬퍼 메서드에서 공유 가능

    // 인증 객체 생성 헬퍼
    private UsernamePasswordAuthenticationToken auth() {
        // CustomUserDetails 생성자 구조에 따라 (userId, authorities) 주입
        CustomUserDetails userDetails = new CustomUserDetails(userId, java.util.List.of());
        return new UsernamePasswordAuthenticationToken(userDetails, null, java.util.List.of());
    }

    @Test
    @DisplayName("잔여 응원 횟수 조회 - 성공")
    void getTodaysCheers_success() throws Exception {
        // given
        CheerStatusDto statusDto = new CheerStatusDto(2, 3, "2026-04-23T00:00:00");
        given(cheerService.getTodaysStatus(userId)).willReturn(statusDto);

        // when & then
        mockMvc.perform(get("/api/v1/cheers/today")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth())) // 이 부분이 핵심!
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usedToday").value(2));
    }

    @Test
    @DisplayName("응원 부여 - 성공")
    void cheerAnimal_success() throws Exception {
        // given
        Long animalId = 100L;
        CheerRes res = new CheerRes(animalId, 10, 36.5, 4);
        given(cheerService.cheerAnimal(userId, animalId)).willReturn(res);

        // when & then
        mockMvc.perform(post("/api/v1/animals/{animalId}/cheers", animalId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth())) // 재사용
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalId").value(animalId));
    }

    @Test
    @DisplayName("응원 부여 실패 - 존재하지 않는 사용자")
    void cheerAnimal_fail_userNotFound() throws Exception {
        // given
        Long animalId = 100L;
        // 서비스에서 USER_NOT_FOUND 예외를 던지도록 설정
        given(cheerService.cheerAnimal(userId, animalId))
                .willThrow(new BusinessException(UserErrorCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/v1/animals/{animalId}/cheers", animalId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // 404 에러 (UserErrorCode 설정에 따름)
                .andExpect(jsonPath("$.code").value("U-004")); // 실제 프로젝트의 에러코드 값으로 수정
    }

    @Test
    @DisplayName("응원 부여 실패 - 존재하지 않는 동물")
    void cheerAnimal_fail_animalNotFound() throws Exception {
        // given
        Long animalId = 999L; // 존재하지 않는 ID 가정
        given(cheerService.cheerAnimal(userId, animalId))
                .willThrow(new BusinessException(AnimalErrorCode.ANIMAL_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/v1/animals/{animalId}/cheers", animalId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("A-001")); // 실제 프로젝트의 에러코드 값으로 수정
    }

    @Test
    @DisplayName("응원 부여 실패 - 일일 응원 제한 초과")
    void cheerAnimal_fail_limitExceeded() throws Exception {
        // given
        Long animalId = 100L;
        given(cheerService.cheerAnimal(userId, animalId))
                .willThrow(new BusinessException(CheerErrorCode.DAILY_CHEER_LIMIT_EXCEEDED));

        // when & then
        mockMvc.perform(post("/api/v1/animals/{animalId}/cheers", animalId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // 400 에러
                .andExpect(jsonPath("$.code").value("CH-001")); // 실제 프로젝트의 에러코드 값으로 수정
    }
}