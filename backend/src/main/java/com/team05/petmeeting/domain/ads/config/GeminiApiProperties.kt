package com.team05.petmeeting.domain.ads.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "gemini.api")
class GeminiApiProperties {
    var key: String? = null
    var url: String = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
}
