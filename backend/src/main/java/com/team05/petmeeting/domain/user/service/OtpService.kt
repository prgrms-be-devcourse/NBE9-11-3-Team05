package com.team05.petmeeting.domain.user.service

import com.team05.petmeeting.domain.user.errorCode.UserErrorCode
import com.team05.petmeeting.global.exception.BusinessException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service

class OtpService(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val COOLDOWN_PREFIX = "otp:cooldown:"
        private const val COOLDOWN_TTL = 60L // seconds

        private const val SIGNUP_PREFIX = "otp:signup:"
        private const val OTP_TTL = 5L // minutes

        private const val ATTEMPT_PREFIX = "otp:attempt:"
        private const val MAX_ATTEMPT = 5

        private const val VERIFIED_PREFIX = "otp:verified:"
        private const val VERIFIED_TTL = 60L // minutes
    }

    fun saveSignupOtp(email: String): String {

        val code = generateOtp()
        redisTemplate.opsForValue()
            .set(SIGNUP_PREFIX + email, code, OTP_TTL, TimeUnit.MINUTES)

        return code

    }

    fun getSignupOtp(email: String): String? = redisTemplate.opsForValue().get(SIGNUP_PREFIX + email)

    fun increaseAttempt(email: String) {
        val key = ATTEMPT_PREFIX + email
        val count = redisTemplate.opsForValue().increment(key)
        if (count == 1L) {
            redisTemplate.expire(key, OTP_TTL, TimeUnit.MINUTES)
        }
    }

    fun isExceededAttempts(email: String): kotlin.Boolean {

        val key = ATTEMPT_PREFIX + email
        val value = redisTemplate.opsForValue().get(key) ?: return false
        return value.toInt() > MAX_ATTEMPT

    }

    fun clearOtp(email: String) {
        redisTemplate.delete(SIGNUP_PREFIX + email)
        redisTemplate.delete(ATTEMPT_PREFIX + email)
    }

    fun markVerifiedWithToken(email: String): String {

        val verifyToken = UUID.randomUUID().toString()
        redisTemplate.opsForValue()
            .set(VERIFIED_PREFIX + verifyToken, email, VERIFIED_TTL, TimeUnit.MINUTES)
        return verifyToken
    }

    fun getEmailByVerifyToken(verifyToken: String): String? =
        redisTemplate.opsForValue().get(VERIFIED_PREFIX + verifyToken)

    fun clearVerifiedByToken(verifyToken: String) {
        redisTemplate.delete(VERIFIED_PREFIX + verifyToken)
    }

    fun checkCooldown(email: String) {

        val key = COOLDOWN_PREFIX + email
        if (redisTemplate.hasKey(key) == true) {
            throw BusinessException(UserErrorCode.TOO_MANY_OTP_REQUEST)
        }

        redisTemplate.opsForValue()
            .set(key, "1", COOLDOWN_TTL, TimeUnit.SECONDS)

    }

    private fun generateOtp(): String = ((Math.random() * 900000).toInt() + 100000).toString()

}