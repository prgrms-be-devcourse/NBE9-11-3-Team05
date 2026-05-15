package com.team05.petmeeting.domain.donation.repository

import com.team05.petmeeting.domain.donation.entity.Donation
import org.springframework.data.jpa.repository.JpaRepository

interface DonationRepository : JpaRepository<Donation?, Long?> {
    fun findByUser_Id(userId: Long?): MutableList<Donation?>?

    fun findByPaymentId(s: String?): Donation?
}
