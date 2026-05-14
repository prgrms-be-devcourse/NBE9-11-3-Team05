package com.team05.petmeeting.domain.cheer.dto

data class CheerStatusDto(
    val usedToday: Long,
    val remainingToday: Int,
    val resetAt: String // "2024-08-10T00:00:00"
)
