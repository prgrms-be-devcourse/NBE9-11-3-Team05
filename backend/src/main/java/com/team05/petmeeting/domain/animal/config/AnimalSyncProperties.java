package com.team05.petmeeting.domain.animal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "animal.sync")
public class AnimalSyncProperties {
    private final Initial initial = new Initial();
    private final Update update = new Update();

    @Getter
    @Setter
    public static class Initial {
        private int numOfRows = 500;
    }

    @Getter
    @Setter
    public static class Update {
        private int numOfRows = 500;
        private long delayMs = 300L;
    }
}
