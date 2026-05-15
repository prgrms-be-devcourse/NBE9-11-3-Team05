package com.team05.petmeeting.domain.animal.dto.external

data class AnimalResponse @JvmOverloads constructor(
    var header: AnimalHeader? = null,
    var body: AnimalBody? = null,
)
