package com.team05.petmeeting.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team05.petmeeting.domain.donation.service.DonationService;
import com.team05.petmeeting.domain.user.dto.profile.*;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.domain.user.service.UserProfileService;
import com.team05.petmeeting.global.exception.BusinessException;
import com.team05.petmeeting.global.security.test.WithCustomUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithCustomUser(userId = 100L)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private DonationService donationService;

    @Autowired
    private ObjectMapper objectMapper;

    // 닉네임 변경 성공
    @Test
    void changeNickname_success() throws Exception {
        NicknameReq req = new NicknameReq("newNick");
        UserProfileRes res = UserProfileRes.builder().nickname("newNick").build();
        when(userProfileService.modifyNickname(100L, "newNick")).thenReturn(res);

        mockMvc.perform(patch("/api/v1/me/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("newNick"));
    }

    // 닉네임 변경 실패 - 빈 닉네임 (400)
    @Test
    void changeNickname_fail_blank() throws Exception {
        NicknameReq req = new NicknameReq("");

        mockMvc.perform(patch("/api/v1/me/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // 닉네임 변경 실패 - 중복 닉네임 (400, U-007)
    @Test
    void changeNickname_fail_duplicate() throws Exception {
        NicknameReq req = new NicknameReq("dupNick");
        when(userProfileService.modifyNickname(100L, "dupNick"))
                .thenThrow(new BusinessException(UserErrorCode.DUPLICATE_NICKNAME));

        mockMvc.perform(patch("/api/v1/me/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // 기본 프로필 조회 성공
    @Test
    void getProfile_success() throws Exception {
        MyProfileDetailRes res = new MyProfileDetailRes(3L, 5L, 2L, 1L);
        when(userProfileService.getMyProfileDetails(100L)).thenReturn(res);

        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedCount").value(3))
                .andExpect(jsonPath("$.cheerCount").value(5));
    }

    // 프로필 조회 실패 - 존재하지 않는 사용자 (404, U-004)
    @Test
    void getProfile_fail_userNotFound() throws Exception {
        when(userProfileService.getUserProfile(100L))
                .thenThrow(new BusinessException(UserErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/me/profile"))
                .andExpect(status().isNotFound());
    }

    // 비밀번호 변경 성공
    @Test
    void changePassword_success() throws Exception {
        PasswordReq req = new PasswordReq("currentPw1!", "NewPassword1!");
        doNothing().when(userProfileService).modifyPassword(100L, "currentPw1!", "NewPassword1!");

        mockMvc.perform(patch("/api/v1/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    // 비밀번호 변경 실패 - 형식 안 맞는 새 비밀번호 (400)
    @Test
    void changePassword_fail_invalidFormat() throws Exception {
        PasswordReq req = new PasswordReq("currentPw1!", "weak");

        mockMvc.perform(patch("/api/v1/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // 비밀번호 변경 실패 - 현재 비밀번호 불일치 (400, U-005)
    @Test
    void changePassword_fail_invalidPassword() throws Exception {
        PasswordReq req = new PasswordReq("wrongPw1!", "NewPassword1!");
        doThrow(new BusinessException(UserErrorCode.INVALID_PASSWORD))
                .when(userProfileService).modifyPassword(100L, "wrongPw1!", "NewPassword1!");

        mockMvc.perform(patch("/api/v1/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // 비밀번호 변경 실패 - 새 비밀번호가 기존과 동일 (400, U-006)
    @Test
    void changePassword_fail_sameAsOld() throws Exception {
        PasswordReq req = new PasswordReq("CurrentPw1!", "CurrentPw1!");
        doThrow(new BusinessException(UserErrorCode.SAME_AS_OLD_PASSWORD))
                .when(userProfileService).modifyPassword(100L, "CurrentPw1!", "CurrentPw1!");

        mockMvc.perform(patch("/api/v1/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // 내 프로필 상세 조회 (응원 수, 동물 수)
    @Test
    void getMyProfile_success() throws Exception {
        MyProfileDetailRes res = MyProfileDetailRes.builder()
                .cheerCount(5L)
                .feedCount(3L)
                .feedCommentCount(5L)
                .animalCommentCount(5L)
                .build();
        when(userProfileService.getMyProfileDetails(100L)).thenReturn(res);

        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cheerCount").value(5))
                .andExpect(jsonPath("$.feedCount").value(3))
                .andExpect(jsonPath("$.feedCommentCount").value(5))
                .andExpect(jsonPath("$.animalCommentCount").value(5))
                ;
    }

    // 비로그인 접근 시 401
    @Test
    @WithAnonymousUser
    void getProfile_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/me/profile"))
                .andExpect(status().isUnauthorized());
    }

    // 후원 목록 조회
    @Test
    void getDonations_success() throws Exception {
        UserDonationRes res = UserDonationRes.builder().build();
        when(donationService.getMyDonations(100L)).thenReturn(res);

        mockMvc.perform(get("/api/v1/me/donations"))
                .andExpect(status().isOk());
    }
}