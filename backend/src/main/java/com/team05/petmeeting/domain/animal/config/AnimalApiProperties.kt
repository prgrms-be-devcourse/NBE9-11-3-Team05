package com.team05.petmeeting.domain.animal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "animal.api")
class AnimalApiProperties {
    var baseUrl: String = ""
    var serviceKey: String = ""
    var returnType: String = ""
    var timeoutMs: Int = 5000
}
