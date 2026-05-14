package com.team05.petmeeting.domain.ads.controller;

import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc  // 이게 있는지 확인
@Transactional
@ActiveProfiles("test")
public class AdsControllerTest {

    @Autowired
    private UserRepository userRepository;
    private Long userId;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        User user = User.create("test@test.com", "테스터", "홍길동");
        user = userRepository.save(user);
        userId = user.getId();
    }

    private UsernamePasswordAuthenticationToken auth() {
        CustomUserDetails userDetails = new CustomUserDetails(userId, List.of());
        return new UsernamePasswordAuthenticationToken(userDetails, null, List.of());
    }

    @Test
    @DisplayName("Top N 동물 조회 성공")
    void getTopAnimals_success() throws Exception {
        mvc.perform(get("/api/v1/ads/top-animals")
                        .param("n", "3")
                        .with(authentication(auth())))  // 인증 추가
                .andExpect(status().isOk());
    }
}