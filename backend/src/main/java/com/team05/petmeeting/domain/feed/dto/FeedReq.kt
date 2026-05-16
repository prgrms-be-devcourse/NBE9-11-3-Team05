package com.team05.petmeeting.domain.feed.dto

import com.team05.petmeeting.domain.feed.enums.FeedCategory

data class FeedReq(
    val category: FeedCategory,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val animalId: Long?
)