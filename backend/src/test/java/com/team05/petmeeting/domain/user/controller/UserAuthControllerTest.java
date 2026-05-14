package com.team05.petmeeting.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team05.petmeeting.domain.user.dto.auth.emailsignup.EmailSignupReq;
import com.team05.petmeeting.domain.user.dto.auth.emailstart.EmailStartReq;
import com.team05.petmeeting.domain.user.dto.auth.emailstart.EmailStartRes;
import com.team05.petmeeting.domain.user.dto.auth.emailstart.EmailStartRes.NextStep;
import com.team05.petmeeting.domain.user.dto.auth.emailverify.EmailVerifyReq;
import com.team05.petmeeting.domain.user.dto.auth.login.AccessTokenRes;
import com.team05.petmeeting.domain.user.dto.auth.login.LoginAndRefreshRes;
import com.team05.petmeeting.domain.user.dto.auth.login.local.EmailLoginReq;
import com.team05.petmeeting.domain.user.service.UserAuthService;
import com.team05.petmeeting.global.security.filter.JwtAuthenticationFilter;
import com.team05.petmeeting.global.security.util.JwtUtil;
import com.team05.petmeeting.global.security.util.RefreshTokenUtil;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WebMvcTest(UserAuthController.class)
class UserAuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean
    UserAuthService userAuthService;
    @MockitoBean
    RefreshTokenUtil refreshTokenUtil;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("이메일 시작 - 성공")
    void startEmail() throws Exception {
        EmailStartReq req = new EmailStartReq("test@test.com");
        EmailStartRes res = new EmailStartRes(true, NextStep.SIGNUP_WITH_OTP);

        Mockito.when(userAuthService.startEmailFlow(anyString()))
                .thenReturn(res);

        mockMvc.perform(post("/api/v1/auth/email/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    @DisplayName("OTP 발송 - 성공")
    void sendOtp() throws Exception {
        EmailStartReq req = new EmailStartReq("test@test.com");

        mockMvc.perform(post("/api/v1/auth/email/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("이메일 인증 - 성공")
    void verifyEmail() throws Exception {
        EmailVerifyReq req = new EmailVerifyReq("test@test.com", "123456");

        Mockito.when(userAuthService.verifyOtp(anyString(), anyString()))
                .thenReturn("verify-token");

        mockMvc.perform(post("/api/v1/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verifyToken").value("verify-token"));
    }

    @Test
    @DisplayName("이메일 회원가입 - 성공")
    void signup() throws Exception {
        String verificationToken = UUID.randomUUID().toString();
        EmailSignupReq req = new EmailSignupReq(verificationToken, "Password12!", "nickname", "realname");

        AccessTokenRes accessToken = new AccessTokenRes("Bearer", "access-token");
        LoginAndRefreshRes result =
                new LoginAndRefreshRes("refreshToken", accessToken);

        Mockito.when(userAuthService.signupAndLoginWithEmail(any()))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/email/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("이메일 로그인 - 성공")
    void login() throws Exception {
        EmailLoginReq req = new EmailLoginReq("test@test.com", "Password12!");

        AccessTokenRes accessToken = new AccessTokenRes("Bearer", "access-token");
        LoginAndRefreshRes result =
                new LoginAndRefreshRes("refershToken", accessToken);

        Mockito.when(userAuthService.loginWithEmail(anyString(), anyString()))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/email/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void logout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("토큰 재발급 - 성공")
    void refresh() throws Exception {
        AccessTokenRes accessToken = new AccessTokenRes("Bearer", "new-access");
        LoginAndRefreshRes result =
                new LoginAndRefreshRes("refreshToken", accessToken);

        Mockito.when(userAuthService.refresh(any()))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }
}