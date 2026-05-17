package com.team05.petmeeting.global.security.oauth

import com.team05.petmeeting.domain.user.refreshtoken.entity.RefreshToken
import com.team05.petmeeting.domain.user.refreshtoken.repository.RefreshTokenRepository
import com.team05.petmeeting.global.security.userdetails.CustomOAuth2User
import com.team05.petmeeting.global.security.util.RefreshTokenUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class OAuth2SuccessHandler(
    private val refreshTokenUtil: RefreshTokenUtil,
    private val refreshTokenRepository: RefreshTokenRepository,
) : SimpleUrlAuthenticationSuccessHandler() {

    @Transactional
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val customUser = authentication.principal as CustomOAuth2User
        val user = customUser.user

        log.info("OAuth login success - userId: {}, email: {}", user.id, user.email)

        val uuid = UUID.randomUUID()
        refreshTokenRepository.save(RefreshToken.create(user, uuid))
        refreshTokenUtil.add(response, uuid.toString())

        response.sendRedirect("http://localhost:3000?oauth=success")
    }

    companion object {
        private val log = LoggerFactory.getLogger(OAuth2SuccessHandler::class.java)
    }
}
