package com.team05.petmeeting.domain.user.entity

import com.team05.petmeeting.domain.shelter.entity.Shelter
import com.team05.petmeeting.domain.user.role.Role
import com.team05.petmeeting.global.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "users")
class User protected constructor() : BaseEntity() {

    @field:OneToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "care_reg_no", referencedColumnName = "care_reg_no")
    var shelter: Shelter? = null
        protected set

    @field:Column(nullable = false, unique = true)
    lateinit var email: String
        protected set

    @field:Column(nullable = false)
    lateinit var nickname: String
        protected set

    @field:Column(nullable = false)
    lateinit var realname: String
        protected set

    @field:Column(nullable = false)
    lateinit var profileImageUrl: String
        protected set

    @field:Column(nullable = false)
    @field:Enumerated(EnumType.STRING)
    lateinit var role: Role
        protected set

    @field:Column(nullable = false)
    var dailyHeartCount: Int = 0
        protected set

    @field:Column(nullable = false)
    lateinit var lastHeartResetDate: LocalDate
        protected set

    @field:OneToMany(
        mappedBy = "user",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    val userAuths: MutableList<UserAuth> = mutableListOf()

    fun addAuth(userAuth: UserAuth) {
        userAuth.user = this
        userAuths.add(userAuth)
    }

    fun resetDailyHeartCountIfNeeded() {
        if (lastHeartResetDate != LocalDate.now()) {
            dailyHeartCount = 0
            lastHeartResetDate = LocalDate.now()
        }
    }

    fun useDailyCheer() {
        dailyHeartCount++
    }

    fun updateProfileImageUrl(profileImageUrl: String) {
        this.profileImageUrl = profileImageUrl
    }

    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    companion object {
        @JvmStatic
        fun create(
            email: String,
            nickname: String,
            realname: String,
        ): User =
            User().apply {
                this.email = email
                this.nickname = nickname
                this.profileImageUrl = ""
                this.realname = realname
                this.role = Role.USER
                this.dailyHeartCount = 0
                this.lastHeartResetDate = LocalDate.now()
            }
    }
}
