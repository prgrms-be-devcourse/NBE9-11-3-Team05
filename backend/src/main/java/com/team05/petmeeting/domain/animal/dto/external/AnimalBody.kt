package com.team05.petmeeting.domain.animal.dto.external

data class AnimalBody @JvmOverloads constructor(
    var items: AnimalItems? = null,
    var numOfRows: Int? = null,
    var pageNo: Int? = null,
    var totalCount: Int? = null,
)
