package com.team05.petmeeting.domain.donation.config

import io.portone.sdk.server.PortOneClient
import io.portone.sdk.server.payment.PaymentClient
import io.portone.sdk.server.webhook.WebhookVerifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PortOneConfig {
    @Value("\${portone.api-secret}")
    lateinit var apiSecret: String

    @Value("\${portone.store-id}")
    lateinit var storeId: String

    @Value("\${portone.webhook-secret}")
    lateinit var webhookSecret: String

    @Bean
    fun portOne() = PaymentClient(apiSecret)


    @Bean
    fun webhookVerifier() = WebhookVerifier(webhookSecret)
}