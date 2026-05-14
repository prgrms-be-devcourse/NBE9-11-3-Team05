package com.team05.petmeeting.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class OtpServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("OTP 저장 테스트")
    void saveSignupOtp() {
        String email = "test@test.com";

        String code = otpService.saveSignupOtp(email);

        assertThat(code).isNotNull();
        verify(valueOperations).set(
                startsWith("otp:signup:"), anyString(), eq(5L), eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("OTP 조회 테스트")
    void getSignupOtp() {
        String email = "test@test.com";
        when(valueOperations.get("otp:signup:" + email)).thenReturn("123456");

        Optional<String> result = otpService.getSignupOtp(email);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("123456");
    }

    @Test
    @DisplayName("시도 횟수 증가 테스트")
    void increaseAttempt() {
        String email = "test@test.com";

        when(valueOperations.increment(anyString())).thenReturn(1L);

        otpService.increaseAttempt(email);

        verify(valueOperations).increment("otp:attempt:" + email);
        verify(redisTemplate).expire("otp:attempt:" + email, 5L, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("시도 횟수 초과 여부 - 초과")
    void isExceededAttempts_true() {
        String email = "test@test.com";

        when(valueOperations.get("otp:attempt:" + email)).thenReturn("6");

        boolean result = otpService.isExceededAttempts(email);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("시도 횟수 초과 여부 - 정상")
    void isExceededAttempts_false() {
        String email = "test@test.com";

        when(valueOperations.get("otp:attempt:" + email)).thenReturn("3");

        boolean result = otpService.isExceededAttempts(email);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("OTP 삭제 테스트")
    void clearOtp() {
        String email = "test@test.com";

        otpService.clearOtp(email);

        verify(redisTemplate).delete("otp:signup:" + email);
        verify(redisTemplate).delete("otp:attempt:" + email);
    }

    @Test
    @DisplayName("인증 토큰 생성 테스트")
    void markVerifiedWithToken() {
        String email = "test@test.com";

        String token = otpService.markVerifiedWithToken(email);

        assertThat(token).isNotNull();
        verify(valueOperations).set(
                startsWith("otp:verified:"), eq(email), eq(60L), eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("토큰으로 이메일 조회")
    void getEmailByVerifyToken() {
        String token = "abc";

        when(valueOperations.get("otp:verified:" + token)).thenReturn("test@test.com");

        Optional<String> result = otpService.getEmailByVerifyToken(token);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("토큰 삭제 테스트")
    void clearVerifiedByToken() {
        String token = "abc";

        otpService.clearVerifiedByToken(token);

        verify(redisTemplate).delete("otp:verified:" + token);
    }

    @Test
    @DisplayName("쿨다운 체크 - 이미 존재하면 예외")
    void checkCooldown_throw() {
        String email = "test@test.com";

        when(redisTemplate.hasKey("otp:cooldown:" + email)).thenReturn(true);

        assertThatThrownBy(() -> otpService.checkCooldown(email))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("쿨다운 체크 - 정상")
    void checkCooldown_success() {
        String email = "test@test.com";

        when(redisTemplate.hasKey("otp:cooldown:" + email)).thenReturn(false);

        otpService.checkCooldown(email);

        verify(valueOperations).set(
                "otp:cooldown:" + email, "1", 60L, TimeUnit.SECONDS
        );
    }
}