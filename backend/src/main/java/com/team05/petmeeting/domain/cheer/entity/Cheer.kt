package com.team05.petmeeting.domain.cheer.entity

import com.team05.petmeeting.domain.animal.entity.Animal
import com.team05.petmeeting.domain.user.entity.User
import com.team05.petmeeting.global.entity.BaseEntity
import jakarta.persistence.*
import lombok.AccessLevel
import lombok.Getter
import lombok.NoArgsConstructor

@Entity
@Table(name = "cheers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Cheer(
    @field:JoinColumn(name = "user_id")
    @field:ManyToOne(fetch = FetchType.LAZY)
    val user: User,
    @field:JoinColumn(name = "animal_id")
    @field:ManyToOne(fetch = FetchType.LAZY)
    val animal: Animal
) : BaseEntity()