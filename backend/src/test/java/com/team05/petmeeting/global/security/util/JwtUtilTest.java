package com.team05.petmeeting.global.security.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        // 테스트용 SecretKey 생성
        secretKey = Keys.hmacShaKeyFor("testtesttesttesttesttesttesttest".getBytes());

        jwtUtil = new JwtUtil(secretKey);

        // expireMillis 강제로 세팅 (리플렉션)
        try {
            var field = JwtUtil.class.getDeclaredField("expireMillis");
            field.setAccessible(true);
            field.set(jwtUtil, 1000L * 60 * 60); // 1시간
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("토큰 생성 테스트")
    void createTokenTest() {
        // given
        Long userId = 1L;
        List<String> roles = List.of("ROLE_USER");

        // when
        String token = jwtUtil.createToken(userId, roles);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("토큰 파싱 테스트")
    void parseTokenTest() {
        // given
        Long userId = 1L;
        List<String> roles = List.of("ROLE_USER");

        String token = jwtUtil.createToken(userId, roles);

        // when
        Claims claims = jwtUtil.parseToken(token);

        // then
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("roles")).isNotNull();
    }

    @Test
    @DisplayName("Authentication 생성 테스트")
    void getAuthenticationTest() {
        // given
        Long userId = 1L;
        List<String> roles = List.of("ROLE_USER");

        String token = jwtUtil.createToken(userId, roles);
        Claims claims = jwtUtil.parseToken(token);

        // when
        Authentication authentication = jwtUtil.getAuthentication(claims);

        // then
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();

        // principal 검증
        var principal = authentication.getPrincipal();
        assertThat(principal).isInstanceOf(com.team05.petmeeting.global.security.userdetails.CustomUserDetails.class);

        // 권한 검증
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }
}