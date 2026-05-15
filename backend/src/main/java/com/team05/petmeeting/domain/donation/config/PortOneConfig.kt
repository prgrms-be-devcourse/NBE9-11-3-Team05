package com.team05.petmeeting.domain.donation.config

import io.portone.sdk.server.PortOneClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PortOneConfig {
    @Value("\${portone.api-secret}")
    lateinit var apiSecret: String

    @Value("\${portone.store-id}")
    lateinit var storeId: String

    @Bean
    fun portOne(): PortOneClient {
        return PortOneClient(apiSecret, "https://api.portone.io", storeId)
    }
}