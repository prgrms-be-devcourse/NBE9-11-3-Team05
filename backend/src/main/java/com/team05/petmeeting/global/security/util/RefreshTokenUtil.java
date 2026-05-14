package com.team05.petmeeting.global.security.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenUtil {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7일

    /**
     * RefreshToken 쿠키 생성 ( 재발급 )
     **/
    public void add(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);

        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만
        cookie.setAttribute("SameSite", "Strict");
        cookie.setPath("/"); // 필요하면 "/auth/refresh"로 좁혀도 됨
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);

        response.addCookie(cookie);
    }

    /**
     * RefreshToken 쿠키 삭제
     */
    public void delete(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);

        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}
