package com.team05.petmeeting.global.security.util;

import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final SecretKey secretKey;

    @Value("${jwt.expireMillis}") // .env
    private long expireMillis;

    public Claims parseToken(String token) {
        // 토큰 만료 or 변조 시 예외 throw
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Authentication getAuthentication(Claims claims) {

        // 토큰 payload 내 사용자 정보 추출
        Long userId = Long.parseLong(claims.getSubject());

        List<String> roles = claims.get("roles", List.class);
        if (roles == null) {
            roles = List.of();
        }
        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // Principal 객체 등록
        UserDetails user =
                new CustomUserDetails(userId, authorities);

        // Authentication 객체
        return new UsernamePasswordAuthenticationToken(
                user, null, authorities
        );
    }

    public String createToken(
            Long userId,
            List<String> roles
    ) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expireMillis);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", roles)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
}
