package com.team05.petmeeting.domain.user.service;

import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.global.exception.BusinessException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    private static final String COOLDOWN_PREFIX = "otp:cooldown:";
    private static final long COOLDOWN_TTL = 60; // seconds

    private static final String SIGNUP_PREFIX = "otp:signup:";
    private static final long OTP_TTL = 5; // minutes

    private static final String ATTEMPT_PREFIX = "otp:attempt:";
    private static final int MAX_ATTEMPT = 5; // minutes

    private static final String VERIFIED_PREFIX = "otp:verified:";
    private static final long VERIFIED_TTL = 60; // minutes

    public String saveSignupOtp(String email) {
        String code = generateOtp();

        redisTemplate.opsForValue()
                .set(SIGNUP_PREFIX + email, code, OTP_TTL, TimeUnit.MINUTES);

        return code;
    }

    public Optional<String> getSignupOtp(String email) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(SIGNUP_PREFIX + email));
    }

    public void increaseAttempt(String email) {
        String key = ATTEMPT_PREFIX + email;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, OTP_TTL, TimeUnit.MINUTES);
        }
    }

    public boolean isExceededAttempts(String email) {
        String key = ATTEMPT_PREFIX + email;

        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return false;
        }

        return Integer.parseInt(value) > MAX_ATTEMPT;
    }

    public void clearOtp(String email) {
        redisTemplate.delete(SIGNUP_PREFIX + email);
        redisTemplate.delete(ATTEMPT_PREFIX + email);
    }

    public String markVerifiedWithToken(String email) {
        String verifyToken = UUID.randomUUID().toString();

        redisTemplate.opsForValue()
                .set(VERIFIED_PREFIX + verifyToken, email, VERIFIED_TTL, TimeUnit.MINUTES);

        return verifyToken;
    }

    public Optional<String> getEmailByVerifyToken(String verifyToken) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(VERIFIED_PREFIX + verifyToken));
    }

    public void clearVerifiedByToken(String verifyToken) {
        redisTemplate.delete(VERIFIED_PREFIX + verifyToken);
    }

    public void checkCooldown(String email) {
        String key = COOLDOWN_PREFIX + email;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            throw new BusinessException(UserErrorCode.TOO_MANY_OTP_REQUEST);
        }

        // cooldown 설정
        redisTemplate.opsForValue()
                .set(key, "1", COOLDOWN_TTL, TimeUnit.SECONDS);
    }

    private String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}