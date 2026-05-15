package com.team05.petmeeting.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.team05.petmeeting.domain.cheer.repository.CheerRepository;
import com.team05.petmeeting.domain.comment.repository.AnimalCommentRepository;
import com.team05.petmeeting.domain.comment.repository.FeedCommentRepository;
import com.team05.petmeeting.domain.feed.repository.FeedRepository;
import com.team05.petmeeting.domain.user.dto.profile.UserProfileRes;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.entity.UserAuth;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.domain.user.provider.Provider;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

class UserProfileServiceTest {

    private static final String CURRENT_PASSWORD = "CurrentPw1!";
    private static final String NEW_PASSWORD = "NewPassword1!";
    private static final String WRONG_PASSWORD = "WrongPw1!";
    private static final String ENCODED_PASSWORD = "encodedPw!";
    private static final String ENCODED_NEW_PASSWORD = "encodedNewPw!";

    @InjectMocks
    private UserProfileService userProfileService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private FeedRepository feedRepository;
    @Mock
    private CheerRepository cheerRepository;
    @Mock
    private AnimalCommentRepository animalCommentRepository;
    @Mock
    private FeedCommentRepository feedCommentRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);

        User user = User.create("test@test.com", "테스터", "홍길동");
        UserAuth userAuth = UserAuth.create(Provider.LOCAL, "test@test.com", ENCODED_PASSWORD);
        user.addAuth(userAuth);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        userId = 1L;
    }

    // 비밀번호 변경 성공
    @Test
    void modifyPassword_success() {

        when(passwordEncoder.matches(CURRENT_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.matches(NEW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertDoesNotThrow(() ->
                userProfileService.modifyPassword(userId, CURRENT_PASSWORD, NEW_PASSWORD)
        );
    }

    // 비밀번호 변경 실패 - 현재 비밀번호 불일치 (U-005)
    @Test
    void modifyPassword_fail_invalidPassword() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                userProfileService.modifyPassword(userId, WRONG_PASSWORD, NEW_PASSWORD)
        );
        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.INVALID_PASSWORD);
    }

    // 비밀번호 변경 실패 - 새 비밀번호가 기존과 동일 (U-006)
    @Test
    void modifyPassword_fail_sameAsOld() {

        when(passwordEncoder.matches(CURRENT_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userProfileService.modifyPassword(userId, CURRENT_PASSWORD, CURRENT_PASSWORD)
        );
        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.SAME_AS_OLD_PASSWORD);
    }

    // 비밀번호 변경 실패 - 소셜 로그인 유저 (U-014)
    @Test
    void modifyPassword_fail_socialUser() {
        User socialUser = User.create("social@test.com", "소셜유저", "홍길동");
        UserAuth socialAuth = UserAuth.create(Provider.GOOGLE, "google-id-123", null);
        socialUser.addAuth(socialAuth);
        ReflectionTestUtils.setField(socialUser, "id", 2L);
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(socialUser));
        Long socialUserId = 2L;

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userProfileService.modifyPassword(socialUserId, CURRENT_PASSWORD, NEW_PASSWORD)
        );
        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.LOCAL_NOT_FOUND);
    }

    // 닉네임 변경 성공
    @Test
    void modifyNickname_success() {
        UserProfileRes res = userProfileService.modifyNickname(userId, "새닉네임");
        assertThat(res.getNickname()).isEqualTo("새닉네임");
    }

    // 존재하지 않는 유저 조회 (U-004)
    @Test
    void getUserProfile_fail_userNotFound() {
        when(userRepository.findById(999L)).thenReturn(java.util.Optional.empty());
        BusinessException ex = assertThrows(BusinessException.class, () ->
                userProfileService.getUserProfile(999L)
        );
        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }
}
