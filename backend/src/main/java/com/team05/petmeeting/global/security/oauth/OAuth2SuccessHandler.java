package com.team05.petmeeting.global.security.oauth;

import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.refreshtoken.entity.RefreshToken;
import com.team05.petmeeting.domain.user.refreshtoken.repository.RefreshTokenRepository;
import com.team05.petmeeting.global.security.userdetails.CustomOAuth2User;
import com.team05.petmeeting.global.security.util.RefreshTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final RefreshTokenUtil refreshTokenUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        // OAuth2User → CustomOAuth2User로 캐스팅
        CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();

        // 실제 User 엔티티 꺼내기
        User user = customUser.getUser();

        log.info("OAuth login success - userId: {}, email: {}", user.getId(), user.getEmail());

        // refresh 토큰 생성 및 db 저장 -> redis 변경 검토
        UUID uuid = UUID.randomUUID();
        RefreshToken saved = RefreshToken.create(user, uuid);
        refreshTokenRepository.save(saved);

        // 쿠키로 Refresh Token 전달
        refreshTokenUtil.add(response, uuid.toString());

        // 4. 프론트로 redirect (accessToken 전달)
        response.sendRedirect("http://localhost:3000?oauth=success");
    }
}