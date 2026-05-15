package com.team05.petmeeting.domain.adoption.dto

import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus

data class AdoptionReviewReq @JvmOverloads constructor(
    val status: AdoptionStatus? = null,
    val rejectionReason: String? = null,
)
