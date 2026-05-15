package com.team05.petmeeting.domain.ads.dto

data class CardNewsResult(
    @JvmField val imageUrl: String?,
    @JvmField val caption: String?
) {
    fun imageUrl(): String? = imageUrl

    fun caption(): String? = caption
}
