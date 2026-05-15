package com.team05.petmeeting.domain.user.dto.profile

import com.team05.petmeeting.domain.comment.entity.AnimalComment
import java.time.LocalDateTime


data class UserAnimalCommentRes(
    val totalCommentCount: Long,
    val comments: MutableList<AnimalCommentItem?>
) {
    data class AnimalCommentItem(
        val feedId: Long,
        val desertionNo: String,
        val content: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            @JvmStatic
            fun from(comment: AnimalComment): AnimalCommentItem {
                return AnimalCommentItem(
                    comment.animal.id,
                    comment.animal.desertionNo,
                    comment.content,
                    comment.createdAt,
                    comment.updatedAt
                )
            }
        }
    }

    companion object {
        @JvmStatic
        fun of(totalCommentCount: Long, commentList: MutableList<AnimalComment>): UserAnimalCommentRes {
            val items = commentList.stream()
                .map { comment: AnimalComment -> AnimalCommentItem.from(comment) }
                .toList()

            return UserAnimalCommentRes(totalCommentCount, items)
        }
    }
}