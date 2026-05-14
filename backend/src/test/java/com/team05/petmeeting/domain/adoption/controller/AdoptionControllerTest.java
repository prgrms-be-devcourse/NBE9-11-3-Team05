package com.team05.petmeeting.domain.adoption.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team05.petmeeting.domain.adoption.dto.request.AdoptionApplyRequest;
import com.team05.petmeeting.domain.adoption.dto.response.AdoptionApplyResponse;
import com.team05.petmeeting.domain.adoption.dto.response.AdoptionDetailResponse;
import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus;
import com.team05.petmeeting.domain.adoption.errorCode.AdoptionErrorCode;
import com.team05.petmeeting.domain.adoption.service.AdoptionService;
import com.team05.petmeeting.global.exception.BusinessException;
import com.team05.petmeeting.global.security.filter.JwtAuthenticationFilter;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import com.team05.petmeeting.global.security.util.JwtUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdoptionController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
// 일반 사용자 입장에서 입양 신청 목록, 상세, 제출, 취소 API를 검증한다.
class AdoptionControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdoptionService adoptionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUpAuthentication() {
        // @AuthenticationPrincipal로 전달될 로그인 사용자를 SecurityContext에 직접 주입한다.
        SecurityContextHolder.getContext().setAuthentication(auth());
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("로그인 사용자의 입양 신청 목록 조회 성공")
    void getMyAdoptions() throws Exception {
        AdoptionApplyResponse response = new AdoptionApplyResponse(
                10L,
                AdoptionStatus.Processing,
                new AdoptionApplyResponse.AnimalInfo("A-001", "믹스견", "테스트 보호소", "보호소장")
        );
        given(adoptionService.getMyAdoptions(USER_ID)).willReturn(List.of(response));

        mockMvc.perform(get("/adoptions/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(10L))
                .andExpect(jsonPath("$[0].status").value("Processing"))
                .andExpect(jsonPath("$[0].animalInfo.desertionNo").value("A-001"))
                .andExpect(jsonPath("$[0].animalInfo.careNm").value("테스트 보호소"));

        verify(adoptionService).getMyAdoptions(USER_ID);
    }

    @Test
    @DisplayName("로그인 사용자의 입양 신청 상세 조회 성공")
    void getApplicationDetail() throws Exception {
        Long applicationId = 10L;
        AdoptionDetailResponse response = new AdoptionDetailResponse(
                applicationId,
                AdoptionStatus.Processing,
                "입양하고 싶습니다.",
                LocalDateTime.of(2026, 4, 23, 10, 0),
                null,
                null,
                "010-1234-5678",
                new AdoptionDetailResponse.AnimalInfo(
                        "A-001",
                        "특이사항 없음",
                        "테스트 보호소",
                        "보호소장",
                        "010-0000-0000",
                        "서울시"
                )
        );
        given(adoptionService.getApplicationDetail(USER_ID, applicationId)).willReturn(response);

        mockMvc.perform(get("/adoptions/{applicationId}", applicationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(applicationId))
                .andExpect(jsonPath("$.status").value("Processing"))
                .andExpect(jsonPath("$.applyReason").value("입양하고 싶습니다."))
                .andExpect(jsonPath("$.applyTel").value("010-1234-5678"))
                .andExpect(jsonPath("$.animalInfo.desertionNo").value("A-001"))
                .andExpect(jsonPath("$.animalInfo.careTel").value("010-0000-0000"));

        verify(adoptionService).getApplicationDetail(USER_ID, applicationId);
    }

    @Test
    @DisplayName("로그인 사용자의 입양 신청 제출 성공")
    void applyApplication() throws Exception {
        // 컨트롤러는 로그인 사용자 ID, 동물 ID, 요청 본문을 서비스에 위임한다.
        Long animalId = 20L;
        AdoptionApplyResponse response = new AdoptionApplyResponse(
                10L,
                AdoptionStatus.Processing,
                new AdoptionApplyResponse.AnimalInfo("A-001", "믹스견", "테스트 보호소", "보호소장")
        );
        given(adoptionService.applyApplication(eq(USER_ID), eq(animalId), any(AdoptionApplyRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/adoptions/{animalId}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applyReason": "가족으로 맞이하고 싶습니다.",
                                  "applyTel": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(10L))
                .andExpect(jsonPath("$.status").value("Processing"))
                .andExpect(jsonPath("$.animalInfo.desertionNo").value("A-001"));

        verify(adoptionService).applyApplication(eq(USER_ID), eq(animalId), any(AdoptionApplyRequest.class));
    }

    @Test
    @DisplayName("이미 신청한 동물은 중복 신청 에러를 반환한다")
    void applyApplication_alreadyApplied() throws Exception {
        // 서비스에서 발생한 도메인 예외가 전역 예외 핸들러를 통해 HTTP 409로 변환되는지 확인한다.
        given(adoptionService.applyApplication(eq(USER_ID), eq(20L), any(AdoptionApplyRequest.class)))
                .willThrow(new BusinessException(AdoptionErrorCode.ALREADY_APPLIED));

        mockMvc.perform(post("/adoptions/{animalId}", 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applyReason": "가족으로 맞이하고 싶습니다.",
                                  "applyTel": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AD-002"))
                .andExpect(jsonPath("$.message").value("이미 입양 신청한 동물입니다."));
    }

    @Test
    @DisplayName("로그인 사용자의 입양 신청 취소 성공")
    void cancelApplication() throws Exception {
        Long applicationId = 10L;
        doNothing().when(adoptionService).cancelApplication(USER_ID, applicationId);

        mockMvc.perform(delete("/adoptions/{applicationId}", applicationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(adoptionService).cancelApplication(USER_ID, applicationId);
    }

    private UsernamePasswordAuthenticationToken auth() {
        CustomUserDetails userDetails = new CustomUserDetails(USER_ID, List.of());
        return new UsernamePasswordAuthenticationToken(userDetails, null, List.of());
    }
}
