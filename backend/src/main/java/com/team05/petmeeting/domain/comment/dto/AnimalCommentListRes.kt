package com.team05.petmeeting.domain.comment.dto

data class AnimalCommentListRes(
    val comments: List<AnimalCommentRes>,
    val totalCount: Int
) {
    companion object {
        @JvmStatic
        fun from(comments: List<AnimalCommentRes>): AnimalCommentListRes {
            return AnimalCommentListRes(comments, comments.size)
        }
    }
}
