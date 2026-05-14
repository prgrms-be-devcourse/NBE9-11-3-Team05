package com.team05.petmeeting.domain.ads.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "instagram")
public class InstagramProperties {
    private String userId;
    private String accessToken;
}