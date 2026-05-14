package com.team05.petmeeting.domain.adoption.dto

data class AdoptionApplyReq @JvmOverloads constructor(
    val applyReason: String? = null,
    val applyTel: String? = null,
)
