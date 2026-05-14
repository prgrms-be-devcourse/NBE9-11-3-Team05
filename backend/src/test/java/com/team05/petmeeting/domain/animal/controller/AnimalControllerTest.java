package com.team05.petmeeting.domain.animal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team05.petmeeting.domain.animal.dto.AnimalRes;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.service.AnimalService;
import com.team05.petmeeting.global.security.filter.JwtAuthenticationFilter;
import com.team05.petmeeting.global.security.util.JwtUtil;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnimalController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalService animalService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("유기동물 리스트 조회 테스트")
    void animalListTest() throws Exception {
        // given
        Animal animal = Animal.builder()
                .desertionNo("12345")
                .processState("보호중")
                .stateGroup(0)
                .noticeNo("공고-1")
                .noticeEdt(LocalDate.now())
                .upKindNm("개")
                .kindFullNm("[개] 믹스견")
                .colorCd("흰색")
                .age("2023(년생)")
                .weight("5kg")
                .sexCd("M")
                .totalCheerCount(10)
                .build();

        AnimalRes animalRes = new AnimalRes(animal);
        Pageable pageable = PageRequest.of(0, 12);
        Page<AnimalRes> page = new PageImpl<>(List.of(animalRes), pageable, 1);

        given(animalService.getAnimals(anyString(), anyString(), anyInt(), any(Pageable.class))).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/animals")
                        .param("region", "서울")
                        .param("kind", "개")
                        .param("stateGroup", "0")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].desertionNo").value("12345"))
                .andExpect(jsonPath("$.content[0].kindFullNm").value("[개] 믹스견"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andDo(print());
    }

    @Test
    @DisplayName("유기동물 상세 조회 테스트")
    void animalDetailTest() throws Exception {
        // given
        Long animalId = 1L;
        Animal animal = Animal.builder()
                .desertionNo("12345")
                .processState("보호중")
                .stateGroup(0)
                .noticeNo("공고-1")
                .noticeEdt(LocalDate.now())
                .upKindNm("개")
                .kindFullNm("[개] 믹스견")
                .colorCd("흰색")
                .age("2023(년생)")
                .weight("5kg")
                .sexCd("M")
                .totalCheerCount(10)
                .build();

        given(animalService.findByAnimalId(eq(animalId))).willReturn(animal);

        // when & then
        mockMvc.perform(get("/api/v1/animals/{animalId}", animalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.desertionNo").value("12345"))
                .andExpect(jsonPath("$.kindFullNm").value("[개] 믹스견"))
                .andExpect(jsonPath("$.totalCheerCount").value(10))
                .andDo(print());
    }
}
