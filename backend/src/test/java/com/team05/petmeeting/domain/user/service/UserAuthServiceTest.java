package com.team05.petmeeting.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team05.petmeeting.domain.user.dto.auth.emailsignup.EmailSignupReq;
import com.team05.petmeeting.domain.user.dto.auth.login.LoginAndRefreshRes;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.entity.UserAuth;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.domain.user.provider.Provider;
import com.team05.petmeeting.domain.user.refreshtoken.entity.RefreshToken;
import com.team05.petmeeting.domain.user.refreshtoken.repository.RefreshTokenRepository;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import com.team05.petmeeting.global.security.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserAuthServiceTest {

    @InjectMocks
    private UserAuthService userAuthService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private OtpService otpService;
    @Mock
    private MailService mailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("verifyOtp - 성공")
    void verifyOtp_success() {
        String email = "test@gmail.com";
        String code = "123456";

        when(otpService.getSignupOtp(email)).thenReturn(code);
        when(otpService.markVerifiedWithToken(email)).thenReturn("verify-token");

        String result = userAuthService.verifyOtp(email, code);

        assertThat(result).isEqualTo("verify-token");
        verify(otpService).clearOtp(email);
    }

    @Test
    @DisplayName("verifyOtp - OTP 없음 (만료)")
    void verifyOtp_expired() {
        String email = "test@gmail.com";

        when(otpService.getSignupOtp(email)).thenReturn(null);

        assertThatThrownBy(() -> userAuthService.verifyOtp(email, "123456"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(UserErrorCode.EXPIRED_OTP);
    }

    @Test
    @DisplayName("verifyOtp - 코드 불일치")
    void verifyOtp_invalid() {
        String email = "test@gmail.com";

        when(otpService.getSignupOtp(email)).thenReturn("123456");
        when(otpService.isExceededAttempts(email)).thenReturn(false);

        assertThatThrownBy(() -> userAuthService.verifyOtp(email, "000000"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(UserErrorCode.INVALID_OTP);

        verify(otpService).increaseAttempt(email);
    }

    @Test
    @DisplayName("verifyOtp - 시도 횟수 초과")
    void verifyOtp_too_many_attempts() {
        String email = "test@gmail.com";

        when(otpService.getSignupOtp(email)).thenReturn("123456");
        when(otpService.isExceededAttempts(email)).thenReturn(true);

        assertThatThrownBy(() -> userAuthService.verifyOtp(email, "000000"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(UserErrorCode.TOO_MANY_OTP_ATTEMPTS);

        verify(otpService).increaseAttempt(email);
        verify(otpService).clearOtp(email);
    }

    @Test
    @DisplayName("signupAndLoginWithEmail - 성공")
    void signup_success() {
        String token = "valid-token";
        EmailSignupReq request = new EmailSignupReq(token, "pw", "닉네임", "홍길동");

        when(otpService.getEmailByVerifyToken(token)).thenReturn("test@gmail.com");
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(null);
        when(passwordEncoder.encode("pw")).thenReturn("encoded");
        when(jwtUtil.createToken(any(), anyList())).thenReturn("accessToken");

        User user = User.create("test@gmail.com", "닉네임", "홍길동");
        when(userRepository.save(any())).thenReturn(user);

        LoginAndRefreshRes result = userAuthService.signupAndLoginWithEmail(request);

        assertThat(result.getAccessTokenRes().getAccessToken()).isEqualTo("accessToken");
        verify(otpService).clearVerifiedByToken(token);
    }

    @Test
    @DisplayName("signupAndLoginWithEmail - 이미 가입된 이메일")
    void signup_fail_duplicate() {
        String token = "token";
        EmailSignupReq request = new EmailSignupReq(token, "pw", "닉네임", "홍길동");
        User user = User.create("test@gmail.com", "닉네임", "홍길동");

        when(otpService.getEmailByVerifyToken(token)).thenReturn("test@gmail.com");
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(user);

        assertThatThrownBy(() -> userAuthService.signupAndLoginWithEmail(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(UserErrorCode.ALREADY_REGISTERED_EMAIL);
    }

    @Test
    @DisplayName("signupAndLoginWithEmail - verification token 없음")
    void signup_fail_invalid_verification_token() {
        String token = "invalid-token";
        EmailSignupReq request = new EmailSignupReq(token, "pw", "닉네임", "홍길동");

        when(otpService.getEmailByVerifyToken(token)).thenReturn(null);

        assertThatThrownBy(() -> userAuthService.signupAndLoginWithEmail(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(UserErrorCode.INVALID_VERIFICATION_TOKEN);
    }

    @Test
    @DisplayName("loginWithEmail - 성공")
    void login_success() {
        String email = "test@gmail.com";

        User user = User.create(email, "닉네임", "홍길동");
        UserAuth auth = UserAuth.create(Provider.LOCAL, email, "encoded");
        user.addAuth(auth);

        when(userRepository.findByEmailWithAuths(email)).thenReturn(user);
        when(passwordEncoder.matches("pw", "encoded")).thenReturn(true);
        when(jwtUtil.createToken(any(), anyList())).thenReturn("accessToken");

        LoginAndRefreshRes result = userAuthService.loginWithEmail(email, "pw");

        assertThat(result.getAccessTokenRes().getAccessToken()).isEqualTo("accessToken");
        verify(refreshTokenRepository).save(any());
    }

    @Test
    @DisplayName("loginWithEmail - 비밀번호 틀림")
    void login_fail_wrong_password() {
        String email = "test@gmail.com";

        User user = User.create(email, "닉네임", "홍길동");
        UserAuth auth = UserAuth.create(Provider.LOCAL, email, "encoded");
        user.addAuth(auth);

        when(userRepository.findByEmailWithAuths(email)).thenReturn(null);
        when(passwordEncoder.matches("pw", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userAuthService.loginWithEmail(email, "pw"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("logout - refreshToken 존재")
    void logout_success() {
        UUID token = UUID.randomUUID();
        Cookie cookie = new Cookie("refreshToken", token.toString());

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        userAuthService.logout(request);

        verify(refreshTokenRepository).deleteByToken(token);
    }

    @Test
    @DisplayName("logout - 쿠키 없음")
    void logout_no_cookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        userAuthService.logout(request);

        verify(refreshTokenRepository, never()).deleteByToken(any());
    }

    @Test
    @DisplayName("refresh - 성공")
    void refresh_success() {
        UUID token = UUID.randomUUID();
        Cookie cookie = new Cookie("refreshToken", token.toString());

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        User user = User.create("test@gmail.com", "닉네임", "홍길동");
        var saved = RefreshToken.create(user, token);

        when(refreshTokenRepository.findByToken(token)).thenReturn(saved);
        when(jwtUtil.createToken(any(), anyList())).thenReturn("newAccess");

        LoginAndRefreshRes result = userAuthService.refresh(request);

        assertThat(result.getAccessTokenRes().getAccessToken()).isEqualTo("newAccess");
        verify(refreshTokenRepository).delete(saved);
        verify(refreshTokenRepository).save(any());
    }
}