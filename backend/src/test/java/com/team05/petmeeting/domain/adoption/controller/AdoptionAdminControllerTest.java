package com.team05.petmeeting.domain.adoption.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team05.petmeeting.domain.adoption.dto.AdoptionReviewReq;
import com.team05.petmeeting.domain.adoption.dto.AdoptionApplyRes;
import com.team05.petmeeting.domain.adoption.dto.AdoptionDetailRes;
import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus;
import com.team05.petmeeting.domain.adoption.errorCode.AdoptionErrorCode;
import com.team05.petmeeting.domain.adoption.service.AdoptionAdminService;
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

@WebMvcTest(AdoptionAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AdoptionAdminControllerTest {

    private static final Long USER_ID = 1L;
    private static final String CARE_REG_NO = "S-001";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdoptionAdminService adoptionAdminService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUpAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(auth());
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("보호소 관리자 입양 신청 목록 조회 성공")
    void getManagedShelterApplications() throws Exception {
        AdoptionApplyRes response = new AdoptionApplyRes(
                10L,
                AdoptionStatus.Processing,
                new AdoptionApplyRes.AnimalInfo("A-001", "믹스견", "담당보호소", "보호소장")
        );
        given(adoptionAdminService.getManagedShelterApplications(USER_ID, CARE_REG_NO))
                .willReturn(List.of(response));

        mockMvc.perform(get("/adoptions/admin/shelters/{careRegNo}/applications", CARE_REG_NO)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(10L))
                .andExpect(jsonPath("$[0].status").value("Processing"))
                .andExpect(jsonPath("$[0].animalInfo.desertionNo").value("A-001"))
                .andExpect(jsonPath("$[0].animalInfo.careNm").value("담당보호소"));

        verify(adoptionAdminService).getManagedShelterApplications(USER_ID, CARE_REG_NO);
    }

    @Test
    @DisplayName("보호소 관리자 입양 신청 상세 조회 성공")
    void getManagedShelterApplicationDetail() throws Exception {
        Long applicationId = 10L;
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 22, 10, 0);
        AdoptionDetailRes response = new AdoptionDetailRes(
                applicationId,
                AdoptionStatus.Processing,
                "입양하고 싶습니다.",
                createdAt,
                null,
                null,
                "010-1234-5678",
                new AdoptionDetailRes.AnimalInfo(
                        "A-001",
                        "특이사항 없음",
                        "담당보호소",
                        "보호소장",
                        "010-0000-0000",
                        "서울시"
                )
        );
        given(adoptionAdminService.getManagedShelterApplicationDetail(USER_ID, CARE_REG_NO, applicationId))
                .willReturn(response);

        mockMvc.perform(get("/adoptions/admin/shelters/{careRegNo}/applications/{applicationId}",
                        CARE_REG_NO,
                        applicationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(applicationId))
                .andExpect(jsonPath("$.status").value("Processing"))
                .andExpect(jsonPath("$.applyReason").value("입양하고 싶습니다."))
                .andExpect(jsonPath("$.applyTel").value("010-1234-5678"))
                .andExpect(jsonPath("$.animalInfo.desertionNo").value("A-001"))
                .andExpect(jsonPath("$.animalInfo.careNm").value("담당보호소"));

        verify(adoptionAdminService).getManagedShelterApplicationDetail(USER_ID, CARE_REG_NO, applicationId);
    }

    @Test
    @DisplayName("보호소 관리자가 아니면 에러 응답을 반환한다")
    void getManagedShelterApplications_unauthorizedShelter() throws Exception {
        given(adoptionAdminService.getManagedShelterApplications(USER_ID, CARE_REG_NO))
                .willThrow(new BusinessException(AdoptionErrorCode.UNAUTHORIZED_SHELTER));

        mockMvc.perform(get("/adoptions/admin/shelters/{careRegNo}/applications", CARE_REG_NO)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AD-003"))
                .andExpect(jsonPath("$.message").value("해당 보호소의 관리자가 아닙니다."));
    }

    @Test
    @DisplayName("보호소 관리자 입양 신청 상태 검토 성공")
    void reviewApplication() throws Exception {
        Long applicationId = 10L;
        AdoptionDetailRes response = new AdoptionDetailRes(
                applicationId,
                AdoptionStatus.Approved,
                "입양하고 싶습니다.",
                LocalDateTime.of(2026, 4, 22, 10, 0),
                LocalDateTime.of(2026, 4, 22, 11, 0),
                null,
                "010-1234-5678",
                new AdoptionDetailRes.AnimalInfo(
                        "A-001",
                        "특이사항 없음",
                        "담당보호소",
                        "보호소장",
                        "010-0000-0000",
                        "서울시"
                )
        );
        given(adoptionAdminService.reviewApplication(
                any(Long.class),
                any(String.class),
                any(Long.class),
                any(AdoptionReviewReq.class)
        )).willReturn(response);

        mockMvc.perform(patch("/adoptions/admin/shelters/{careRegNo}/applications/{applicationId}/review",
                        CARE_REG_NO,
                        applicationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"Approved","rejectionReason":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(applicationId))
                .andExpect(jsonPath("$.status").value("Approved"))
                .andExpect(jsonPath("$.reviewedAt").isNotEmpty())
                .andExpect(jsonPath("$.rejectionReason").doesNotExist());

        verify(adoptionAdminService).reviewApplication(
                any(Long.class),
                any(String.class),
                any(Long.class),
                any(AdoptionReviewReq.class)
        );
    }

    private UsernamePasswordAuthenticationToken auth() {
        CustomUserDetails userDetails = new CustomUserDetails(USER_ID, List.of());
        return new UsernamePasswordAuthenticationToken(userDetails, null, List.of());
    }
}
