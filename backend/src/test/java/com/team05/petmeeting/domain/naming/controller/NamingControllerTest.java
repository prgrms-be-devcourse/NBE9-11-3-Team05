package com.team05.petmeeting.domain.naming.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team05.petmeeting.domain.naming.dto.*;
import com.team05.petmeeting.domain.naming.service.NamingService;
import com.team05.petmeeting.global.security.filter.JwtAuthenticationFilter;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import com.team05.petmeeting.global.security.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NamingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NamingService namingService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private CustomUserDetails createTestUser() {
        return new CustomUserDetails(1L, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("이름 후보 조회 테스트")
    void getNameCandidatesTest() throws Exception {
        // given
        Long animalId = 1L;
        NameCandidateRes response = new NameCandidateRes(
                animalId, "초코",
                List.of(new NameCandidateRes.CandidateDto(1L, 1L, "바둑이", "유저1", 10, false)),
                1
        );
        given(namingService.getCandidates(eq(animalId), anyLong())).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/naming/animals/{animalId}/candidates", animalId)
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalId").value(animalId))
                .andExpect(jsonPath("$.candidateDtoList[0].proposedName").value("바둑이"))
                .andDo(print());
    }

    @Test
    @DisplayName("새로운 이름 제안 테스트")
    void proposeNameTest() throws Exception {
        // given
        Long animalId = 1L;
        String proposedName = "코코";
        NameProposalReq request = new NameProposalReq(proposedName);
        NameProposalRes response = new NameProposalRes(1L, proposedName);

        given(namingService.proposeName(eq(animalId), anyLong(), eq(proposedName))).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/naming/animals/{animalId}/propose", animalId)
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proposedName").value(proposedName))
                .andDo(print());
    }

    @Test
    @DisplayName("기존 이름 투표 테스트")
    void voteNameTest() throws Exception {
        // given
        Long candidateId = 1L;
        doNothing().when(namingService).vote(eq(candidateId), anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/naming/candidates/{candidateId}/vote", candidateId)
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("금칙어 목록 조회 테스트")
    void getBadWordsTest() throws Exception {
        // given
        BadWordListRes response = new BadWordListRes(
                List.of(new BadWordListRes.BadWordDto(1L, "나쁜말", LocalDateTime.now().toString())),
                1
        );
        given(namingService.getBadWords()).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/naming/admin/badwords")
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.badWords[0].word").value("나쁜말"))
                .andDo(print());
    }

    @Test
    @DisplayName("금칙어 추가 테스트")
    void addBadWordTest() throws Exception {
        // given
        String word = "비속어";
        BadWordAddRes response = new BadWordAddRes(1L, word, LocalDateTime.now());

        given(namingService.addBadWord(eq(word))).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/naming/admin/badwords")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new java.util.HashMap<String, String>() {{
                    put("badWord", "비속어");
                }})));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badWord").value(word))
                .andDo(print());
    }

    @Test
    @DisplayName("금칙어 삭제 테스트")
    void deleteBadWordTest() throws Exception {
        // given
        Long badwordId = 1L;
        doNothing().when(namingService).deleteBadWord(badwordId);

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/naming/admin/badwords/{badwordId}", badwordId)
                );

        // then
        resultActions
                .andExpect(status().isNoContent())
                .andDo(print());
    }
}
