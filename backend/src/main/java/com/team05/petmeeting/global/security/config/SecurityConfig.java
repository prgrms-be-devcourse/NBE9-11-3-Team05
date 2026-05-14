package com.team05.petmeeting.global.security.config;

import com.team05.petmeeting.global.security.filter.JwtAuthenticationFilter;
import com.team05.petmeeting.global.security.handler.JwtAccessDeniedHandler;
import com.team05.petmeeting.global.security.handler.JwtAuthenticationEntryPoint;
import com.team05.petmeeting.global.security.oauth.CustomOAuth2UserService;
import com.team05.petmeeting.global.security.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity // Security 관련 설정 진행
@EnableMethodSecurity
public class SecurityConfig {

    // Custom 예외 응답 등록
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    // Jwt 인증 필터
    private final JwtAuthenticationFilter jwtFilter;
    // 소셜 로그인
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults()) // cors 설정 적용
                .csrf(AbstractHttpConfigurer::disable) // csrf 비활성
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 비활성
                .httpBasic(AbstractHttpConfigurer::disable) // 기본 인증 비활성
                .sessionManagement(session -> session // 세션 미사용 설정
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                );

        // Custom 예외 응답 등록
        http
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );

        // 소셜 로그인
        http
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                );

        // 경로별 인가 작업 : white list
        http
                .authorizeHttpRequests(auth -> auth
                        // 미인가 접근 허용
//                        .requestMatchers("/**").permitAll()  // 테스트용
                        .requestMatchers(
                                "/api/v1/auth/email/**",
                                "/api/v1/auth/logout", // 로그아웃
                                "/api/v1/auth/refresh", // 토큰 재발급
                                "/swagger-ui/**",      // swagger
                                "/v3/api-docs/**"      // swagger
                        ).permitAll()

                        // 조회 API 미인가 접근 허용
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/animals",
                                "/api/v1/animals/**",
                                "/api/v1/animals/*/hearts",
                                "/api/v1/feeds",
                                "/api/v1/feeds/**",
                                "/api/v1/shelters/*/campaign",
                                "/api/v1/campaigns",
                                "/api/v1/shelters/*"
                        ).permitAll()

                        // 외부 API 적재용 엔드포인트 임시 공개
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/animals/sync",
                                "/api/v1/animals/sync/initial",
                                "/api/v1/animals/sync/update",
                                "/api/v1/ads/run"
                        ).permitAll()

                        .requestMatchers("/api/v1/me/**").authenticated()

                        // 그 외 요청에 대해 인증된 접근만 허용
                        .anyRequest().authenticated());

        // jwt 인증 필터 등록
        http
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // pw 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 로그인 인증 매니저 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
