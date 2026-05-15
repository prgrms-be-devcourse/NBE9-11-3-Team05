package com.team05.petmeeting.domain.shelter.repository

import com.team05.petmeeting.domain.shelter.entity.Shelter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShelterRepository : JpaRepository<Shelter, String> {
    fun findByCareRegNoIn(ids: Set<String>): List<Shelter>
}
