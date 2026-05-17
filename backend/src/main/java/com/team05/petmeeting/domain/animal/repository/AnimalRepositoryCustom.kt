package com.team05.petmeeting.domain.animal.repository

import com.team05.petmeeting.domain.animal.entity.Animal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface AnimalRepositoryCustom {
    fun findAnimalsWithFilter(
        region: String?,
        kind: String?,
        stateGroup: Int?,
        pageable: Pageable
    ): Page<Animal>
}
