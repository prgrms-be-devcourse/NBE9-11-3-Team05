package com.team05.petmeeting.domain.comment.dto

data class FeedCommentListRes(
    val comments: List<FeedCommentRes>,
    val totalCount: Int
) {
    companion object {
        @JvmStatic
        fun from(comments: List<FeedCommentRes>): FeedCommentListRes {
            return FeedCommentListRes(comments, comments.size)
        }
    }
}
