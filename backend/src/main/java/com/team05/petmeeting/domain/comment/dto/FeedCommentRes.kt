package com.team05.petmeeting.domain.comment.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.team05.petmeeting.domain.comment.entity.FeedComment
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FeedCommentRes(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val commentId: Long,
    val content: String,
    val feedId: Long,
    val createdAt: LocalDateTime
) {
    companion object {
        @JvmStatic
        fun from(comment: FeedComment): FeedCommentRes {
            return FeedCommentRes(
                comment.user.id,
                comment.user.nickname,
                comment.user.profileImageUrl,
                comment.id,
                comment.content,
                comment.feed.id,
                comment.createdAt
            )
        }
    }
}
