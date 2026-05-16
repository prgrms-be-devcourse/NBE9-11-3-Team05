package com.team05.petmeeting.domain.campaign.repository

import com.team05.petmeeting.domain.campaign.entity.Campaign
import com.team05.petmeeting.domain.campaign.enums.CampaignStatus
import com.team05.petmeeting.domain.shelter.entity.Shelter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface CampaignRepository : JpaRepository<Campaign, Long> {
    // Shelter의 PK 필드명이 careRegNo인 경우
    @Query(
        value = "SELECT * FROM campaigns WHERE care_reg_no = :#{#shelter.careRegNo} " +
                "ORDER BY FIELD(status, 'ACTIVE', 'COMPLETE', 'CLOSED')", nativeQuery = true
    )
    fun findByShelterOrderByStatusNative(@Param("shelter") shelter: Shelter): List<Campaign>
    fun findByShelter(shelter: Shelter): List<Campaign>

    fun existsByShelter_CareRegNoAndStatus(shelterId: String, campaignStatus: CampaignStatus): Boolean

    fun findByShelter_CareRegNoAndStatus(shelterId: String, campaignStatus: CampaignStatus): Optional<Campaign>
}
