package com.team05.petmeeting.global.security.util;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class RefreshTokenUtilTest {

    private final RefreshTokenUtil refreshTokenUtil = new RefreshTokenUtil();

    @Test
    @DisplayName("refresh token 쿠키 생성 테스트")
    void addTest() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        String refreshToken = "test-refresh-token";

        // when
        refreshTokenUtil.add(response, refreshToken);

        // then
        Cookie cookie = response.getCookie(RefreshTokenUtil.REFRESH_TOKEN_COOKIE_NAME);

        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo(refreshToken);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(7 * 24 * 60 * 60);
    }

    @Test
    @DisplayName("refresh token 쿠키 삭제 테스트")
    void deleteTest() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        refreshTokenUtil.delete(response);

        // then
        Cookie cookie = response.getCookie(RefreshTokenUtil.REFRESH_TOKEN_COOKIE_NAME);

        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(0);
    }
}