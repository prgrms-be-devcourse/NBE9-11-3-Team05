package com.team05.petmeeting.domain.user.dto.profile

import com.team05.petmeeting.domain.comment.entity.FeedComment
import com.team05.petmeeting.domain.feed.enums.FeedCategory
import java.time.LocalDateTime


data class UserFeedCommentRes(
    val totalCommentCount: Long,
    val comments: MutableList<ProfileCommentItem>
) {

    data class ProfileCommentItem(
        val feedId: Long,
        val category: FeedCategory,
        val content: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            @JvmStatic
            fun from(comment: FeedComment): ProfileCommentItem {
                return ProfileCommentItem(
                    comment.feed.id,
                    comment.feed.category,
                    comment.content,
                    comment.createdAt,
                    comment.updatedAt
                )
            }
        }
    }

    companion object {
        @JvmStatic
        fun of(totalCommentCount: Long, commentList: MutableList<FeedComment>): UserFeedCommentRes {
            val items = commentList.stream()
                .map { comment: FeedComment -> ProfileCommentItem.from(comment) }
                .toList()

            return UserFeedCommentRes(totalCommentCount, items)
        }
    }
}