package com.team05.petmeeting.domain.user.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team05.petmeeting.domain.donation.service.DonationService;
import com.team05.petmeeting.domain.user.dto.profile.MyProfileDetailRes;
import com.team05.petmeeting.domain.user.dto.profile.NicknameReq;
import com.team05.petmeeting.domain.user.dto.profile.PasswordReq;
import com.team05.petmeeting.domain.user.dto.profile.UserDonationRes;
import com.team05.petmeeting.domain.user.dto.profile.UserProfileRes;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.domain.user.service.UserProfileService;
import com.team05.petmeeting.global.exception.BusinessException;
import com.team05.petmeeting.global.security.filter.JwtAuthenticationFilter;
import com.team05.petmeeting.global.security.test.WithCustomUser;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithCustomUser(userId = 100L)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private DonationService donationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    // 닉네임 변경 성공
    @Test
    void changeNickname_success() throws Exception {
        NicknameReq req = new NicknameReq("newNick");
        User user = User.create("email", "newNick", "name");
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());
        UserProfileRes res = UserProfileRes.from(user);
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
        MyProfileDetailRes res = MyProfileDetailRes.of(5L, 3L, 5L, 5L);

        when(userProfileService.getMyProfileDetails(100L)).thenReturn(res);

        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cheerCount").value(3))
                .andExpect(jsonPath("$.feedCount").value(5))
                .andExpect(jsonPath("$.feedCommentCount").value(5))
                .andExpect(jsonPath("$.animalCommentCount").value(5))
        ;
    }

    // 후원 목록 조회
    @Test
    void getDonations_success() throws Exception {
        UserDonationRes res = Mockito.mock(UserDonationRes.class);
        when(donationService.getMyDonations(100L)).thenReturn(res);

        mockMvc.perform(get("/api/v1/me/donations"))
                .andExpect(status().isOk());
    }
}
