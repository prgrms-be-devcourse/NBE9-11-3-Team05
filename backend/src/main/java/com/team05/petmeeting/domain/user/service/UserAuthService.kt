package com.team05.petmeeting.domain.user.service

import com.team05.petmeeting.domain.user.dto.auth.emailsignup.EmailSignupReq
import com.team05.petmeeting.domain.user.dto.auth.emailstart.EmailStartRes
import com.team05.petmeeting.domain.user.dto.auth.emailstart.EmailStartRes.NextStep
import com.team05.petmeeting.domain.user.dto.auth.login.AccessTokenRes
import com.team05.petmeeting.domain.user.dto.auth.login.LoginAndRefreshRes
import com.team05.petmeeting.domain.user.entity.User
import com.team05.petmeeting.domain.user.entity.UserAuth
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode
import com.team05.petmeeting.domain.user.provider.Provider
import com.team05.petmeeting.domain.user.refreshtoken.entity.RefreshToken
import com.team05.petmeeting.domain.user.refreshtoken.repository.RefreshTokenRepository
import com.team05.petmeeting.domain.user.repository.UserRepository
import com.team05.petmeeting.global.exception.BusinessException
import com.team05.petmeeting.global.security.errorCode.SecurityErrorCode
import com.team05.petmeeting.global.security.util.JwtUtil
import com.team05.petmeeting.global.security.util.RefreshTokenUtil.REFRESH_TOKEN_COOKIE_NAME
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class UserAuthService(
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val mailService: MailService,
    private val otpService: OtpService,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    @Transactional(readOnly = true)
    fun startEmailFlow(email: String): EmailStartRes =
        userRepository.findByEmailWithAuths(email)
            .map { user ->
                val hasLocalAuth = user.userAuths.any { it.provider == Provider.LOCAL }
                EmailStartRes(
                    true,
                    if (hasLocalAuth) NextStep.LOGIN_PASSWORD else NextStep.SOCIAL_LOGIN_ONLY,
                )
            }
            .orElseGet { EmailStartRes(false, NextStep.SIGNUP_WITH_OTP) }

    fun sendSignupOtp(email: String) {
        otpService.checkCooldown(email)
        mailService.sendMail(email, otpService.saveSignupOtp(email))
    }

    fun verifyOtp(email: String, code: String): String {
        val savedCode = otpService.getSignupOtp(email)
            ?: throw BusinessException(UserErrorCode.EXPIRED_OTP)

        if (savedCode != code) {
            otpService.increaseAttempt(email)

            if (otpService.isExceededAttempts(email)) {
                otpService.clearOtp(email)
                throw BusinessException(UserErrorCode.TOO_MANY_OTP_ATTEMPTS)
            }

            throw BusinessException(UserErrorCode.INVALID_OTP)
        }

        otpService.clearOtp(email)
        return otpService.markVerifiedWithToken(email)
    }

    fun signupAndLoginWithEmail(request: EmailSignupReq): LoginAndRefreshRes {
        val email = otpService.getEmailByVerifyToken(request.verificationToken)
            ?: throw BusinessException(UserErrorCode.INVALID_VERIFICATION_TOKEN)

        if (userRepository.findByEmail(email).isPresent) {
            throw BusinessException(UserErrorCode.ALREADY_REGISTERED_EMAIL)
        }

        val user = User.create(email, request.nickname, request.realname).apply {
            addAuth(UserAuth.create(Provider.LOCAL, email, passwordEncoder.encode(request.password)))
        }

        return userRepository.save(user)
            .also { otpService.clearVerifiedByToken(request.verificationToken) }
            .let(::issueToken)
    }

    fun loginWithEmail(email: String, password: String): LoginAndRefreshRes {
        val user = userRepository.findByEmailWithAuths(email)
            .orElseThrow { BusinessException(UserErrorCode.LOGIN_FAILED) }

        val auth = user.userAuths
            .firstOrNull { it.provider == Provider.LOCAL }
            ?: throw BusinessException(UserErrorCode.LOGIN_FAILED)

        if (!passwordEncoder.matches(password, auth.password)) {
            throw BusinessException(UserErrorCode.LOGIN_FAILED)
        }

        return issueToken(user)
    }

    fun logout(request: HttpServletRequest) {
        log.info("------------------------- logout start -------------------------")
        request.extractRefreshToken()
            ?.let(UUID::fromString)
            ?.let(refreshTokenRepository::deleteByToken)
    }

    fun refresh(request: HttpServletRequest): LoginAndRefreshRes {
        val refreshToken = request.extractRefreshToken()
            ?: throw BusinessException(SecurityErrorCode.INVALID_TOKEN)

        val savedToken = refreshTokenRepository.findByToken(UUID.fromString(refreshToken))
            .orElseThrow { BusinessException(SecurityErrorCode.INVALID_TOKEN) }

        if (savedToken.expiresAt.isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(savedToken)
            throw BusinessException(SecurityErrorCode.INVALID_TOKEN)
        }

        val user = savedToken.user
        val newAccessToken = jwtUtil.createToken(user.id, listOf(user.role.name))

        refreshTokenRepository.delete(savedToken)

        val uuid = UUID.randomUUID()
        refreshTokenRepository.save(RefreshToken.create(user, uuid))

        return LoginAndRefreshRes(
            uuid.toString(),
            AccessTokenRes("Bearer", newAccessToken),
        )
    }

    fun withdraw(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(UserErrorCode.USER_NOT_FOUND) }

        refreshTokenRepository.deleteAllByUser(user)
        userRepository.delete(user)
    }

    private fun issueToken(user: User): LoginAndRefreshRes {
        val accessToken = jwtUtil.createToken(user.id, listOf(user.role.name))
        val uuid = UUID.randomUUID()

        refreshTokenRepository.save(RefreshToken.create(user, uuid))

        return LoginAndRefreshRes(
            uuid.toString(),
            AccessTokenRes("Bearer", accessToken),
        )
    }

    private fun HttpServletRequest.extractRefreshToken(): String? =
        cookies
            ?.firstOrNull { it.name == REFRESH_TOKEN_COOKIE_NAME }
            ?.value

    companion object {
        private val log = LoggerFactory.getLogger(UserAuthService::class.java)
    }
}
