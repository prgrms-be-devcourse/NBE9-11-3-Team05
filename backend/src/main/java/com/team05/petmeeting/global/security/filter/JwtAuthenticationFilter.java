package com.team05.petmeeting.global.security.filter;

import com.team05.petmeeting.global.security.handler.JwtAuthenticationEntryPoint;
import com.team05.petmeeting.global.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtAuthenticationEntryPoint entryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 토큰 헤더 형식 검증
            String token = resolveToken(request);

            // 토큰 형식 검증 실패 or 이미 인증 등록 시 무시
            if (token == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰 유효성 검증
            Claims claims = jwtUtil.parseToken(token);

            // 사용자 정보를 보관하는 Authentication 생성
            Authentication auth =
                    jwtUtil.getAuthentication(claims);

            // SecurityContextHolder에 사용자 인증 정보 보관
            SecurityContextHolder.getContext()
                    .setAuthentication(auth);

        } catch (ExpiredJwtException ex) { // 토큰 만료
            entryPoint.commence(
                    request,
                    response,
                    new CredentialsExpiredException("토큰 만료")
            );
            return;

        } catch (JwtException ex) { // 토큰 변조, 구조 이상
            entryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("유효하지 않은 토큰")
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/logout")
                || path.contains("/refresh");
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (bearer == null || !bearer.startsWith("Bearer ")) {
            return null;
        }

        return bearer.replace("Bearer ", "");
    }
}
