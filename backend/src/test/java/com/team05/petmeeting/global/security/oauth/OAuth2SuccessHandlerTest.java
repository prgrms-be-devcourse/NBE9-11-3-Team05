package com.team05.petmeeting.global.security.oauth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.refreshtoken.entity.RefreshToken;
import com.team05.petmeeting.domain.user.refreshtoken.repository.RefreshTokenRepository;
import com.team05.petmeeting.global.security.userdetails.CustomOAuth2User;
import com.team05.petmeeting.global.security.util.RefreshTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

class OAuth2SuccessHandlerTest {

    @Mock
    private RefreshTokenUtil refreshTokenUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OAuth2SuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void OAuth2_로그인_성공시_리프레시토큰_생성_쿠키설정_리다이렉트() throws IOException {
        // given
        User user = User.create("test@test.com", "nick", "real");

        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUser()).thenReturn(user);
        when(authentication.getPrincipal()).thenReturn(customOAuth2User);

        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then

        // 1. refreshToken 저장 호출
        verify(refreshTokenRepository, times(1))
                .save(any(RefreshToken.class));

        // 2. 쿠키 설정 호출
        verify(refreshTokenUtil, times(1))
                .add(eq(response), any(String.class));

        // 3. redirect 호출
        verify(response, times(1))
                .sendRedirect("http://localhost:3000?oauth=success");
    }
}