package com.team05.petmeeting.global.security.config;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtSecretConfig {

    @Value("${jwt.secret}") // .env의 환경 변수 읽어오기
    private String secret;

    @Bean
    public SecretKey secretKey() {
        byte[] keyBytes = secret
                .getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
