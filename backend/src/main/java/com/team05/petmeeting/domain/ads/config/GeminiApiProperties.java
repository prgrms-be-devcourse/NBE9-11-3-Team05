package com.team05.petmeeting.domain.ads.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gemini.api")
@Getter @Setter
public class GeminiApiProperties {
    private String key;
    private String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
}