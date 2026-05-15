package com.team05.petmeeting.domain.donation.dto

@JvmRecord
data class PrepareRes(
    val paymentId: String?,
    val amount: Int
)
