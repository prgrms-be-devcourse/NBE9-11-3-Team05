package com.team05.petmeeting.domain.animal.dto.external

data class AnimalHeader @JvmOverloads constructor(
    var reqNo: Long? = null,
    var resultCode: String? = null,
    var resultMsg: String? = null,
)
