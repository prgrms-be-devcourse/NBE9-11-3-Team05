package com.team05.petmeeting.domain.comment.repository

import com.team05.petmeeting.domain.comment.entity.AnimalComment
import com.team05.petmeeting.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface AnimalCommentRepository : JpaRepository<AnimalComment, Long> {
    fun findByAnimal_Id(animalId: Long): MutableList<AnimalComment>

    fun findAllByUserOrderByCreatedAtDesc(user: User): MutableList<AnimalComment>

    fun countAnimalCommentByUser(user: User): Long
}
