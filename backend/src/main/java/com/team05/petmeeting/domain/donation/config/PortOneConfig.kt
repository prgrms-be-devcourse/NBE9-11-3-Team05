package com.team05.petmeeting.domain.donation.config

import io.portone.sdk.server.PortOneClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PortOneConfig {
    @Value("\${portone.api-secret}")
    private val apiSecret: String? = null

    @Value("\${portone.store-id}")
    private val storeId: String? = null

    @Bean
    fun portOne(): PortOneClient {
        return PortOneClient(apiSecret!!, "https://api.portone.io", storeId)
    }
}