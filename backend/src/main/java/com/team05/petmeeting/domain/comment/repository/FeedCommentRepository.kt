package com.team05.petmeeting.domain.comment.repository

import com.team05.petmeeting.domain.comment.entity.FeedComment
import com.team05.petmeeting.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface FeedCommentRepository : JpaRepository<FeedComment, Long> {
    fun findByFeed_Id(feedId: Long): MutableList<FeedComment>

    fun findAllByUserOrderByCreatedAtDesc(user: User): MutableList<FeedComment>

    fun countFeedCommentByUser(user: User): Long
}