package com.team05.petmeeting.global.security.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        // cors 허용 설정 정보 지정
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        // 쿠커, Authorization 헤더 허용
        config.setAllowCredentials(true);
        // 응답 헤더에 JWT 토큰 담아보낼 경우 브라우저가 읽을 수 있도록 설정
        // config.setExposedHeaders(List.of("Authorization"));
        // 실무에서는 백에서 엑세스 토큰 발급시에도 Authorization 헤더에 담아 보낸다고 하지만
        // 일단 응답 바디에 엑세스 토큰 담아 보내는 식으로 구현하겠습니다 !!

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
