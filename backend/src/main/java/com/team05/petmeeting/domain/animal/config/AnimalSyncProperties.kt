package com.team05.petmeeting.domain.animal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "animal.sync")
class AnimalSyncProperties {
    var initial: Initial = Initial()
    var update: Update = Update()

    class Initial {
        var numOfRows: Int = 500
    }

    class Update {
        var numOfRows: Int = 500
        var delayMs: Long = 300L
    }
}
