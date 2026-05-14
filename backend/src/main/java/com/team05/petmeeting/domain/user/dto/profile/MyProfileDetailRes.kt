package com.team05.petmeeting.domain.user.dto.profile

data class MyProfileDetailRes(
    val feedCount: Long,
    val cheerCount: Long,
    val feedCommentCount: Long,
    val animalCommentCount: Long
) {
    companion object {
        @JvmStatic
        fun of(
            feedCount: Long,
            cheerCount: Long,
            feedCommentCount: Long,
            animalCommentCount: Long
        ): MyProfileDetailRes {
            return MyProfileDetailRes(feedCount, cheerCount, feedCommentCount, animalCommentCount)
        }
    }
}
