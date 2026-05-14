package com.team05.petmeeting.domain.user.service;

import com.team05.petmeeting.domain.user.dto.profile.UserProfileRes;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.entity.UserAuth;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.domain.user.provider.Provider;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import com.team05.petmeeting.global.security.test.WithCustomUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithCustomUser(userId = 100L)
class UserProfileServiceTest {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long userId;

    @BeforeEach
    void setUp() {
        User user = User.create("test@test.com", "테스터", "홍길동");
        UserAuth userAuth = UserAuth.create(Provider.LOCAL, "test@test.com", passwordEncoder.encode("CurrentPw1!"));
        user.addAuth(userAuth);
        user = userRepository.save(user);
        userId = user.getId();
    }

    // 비밀번호 변경 성공
    @Test
    void modifyPassword_success() {
        assertDoesNotThrow(() ->
                userProfileService.modifyPassword(userId, "CurrentPw1!", "NewPassword1!")
        );
    }

    // 비밀번호 변경 실패 - 현재 비밀번호 불일치 (U-005)
    @Test
    void modifyPassword_fail_invalidPassword() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                userProfileService.modifyPassword(userId, "WrongPw1!", "NewPassword1!")
        );
        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.INVALID_PASSWORD);
    }

    // 비밀번호 변경 실패 - 새 비밀번호가 기존과 동일 (U-006)
    @Test
    void modifyPassword_fail_sameAsOld() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                userProfileService.modifyPassword(userId, "CurrentPw1!", "CurrentPw1!")
        );
        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.SAME_AS_OLD_PASSWORD);
    }

    // 비밀번호 변경 실패 - 소셜 로그인 유저 (U-014)
    @Test
    void modifyPassword_fail_socialUser() {
        User socialUser = User.create("social@test.com", "소셜유저", "홍길동");
        UserAuth socialAuth = UserAuth.create(Provider.GOOGLE, "google-id-123", null);
        socialUser.addAuth(socialAuth);
        socialUser = userRepository.save(socialUser);
        Long socialUserId = socialUser.getId();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userProfileService.modifyPassword(socialUserId, "CurrentPw1!", "NewPassword1!")
        );
        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.LOCAL_NOT_FOUND);
    }

    // 닉네임 변경 성공
    @Test
    void modifyNickname_success() {
        UserProfileRes res = userProfileService.modifyNickname(userId, "새닉네임");
        assertThat(res.nickname()).isEqualTo("새닉네임");
    }

    // 존재하지 않는 유저 조회 (U-004)
    @Test
    void getUserProfile_fail_userNotFound() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                userProfileService.getUserProfile(999L)
        );
        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }
}