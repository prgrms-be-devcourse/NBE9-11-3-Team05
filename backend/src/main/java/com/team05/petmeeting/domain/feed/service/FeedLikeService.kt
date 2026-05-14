package com.team05.petmeeting.domain.feed.service

import com.team05.petmeeting.domain.feed.dto.FeedLikeRes
import com.team05.petmeeting.domain.feed.entity.FeedLike
import com.team05.petmeeting.domain.feed.errorCode.FeedErrorCode
import com.team05.petmeeting.domain.feed.repository.FeedLikeRepository
import com.team05.petmeeting.domain.feed.repository.FeedRepository
import com.team05.petmeeting.domain.user.entity.User
import com.team05.petmeeting.global.exception.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedLikeService(
    private val feedLikeRepository: FeedLikeRepository,
    private val feedRepository: FeedRepository
) {

    @Transactional
    fun toggleLike(feedId: Long, user: User): FeedLikeRes {

        val feed = feedRepository.findById(feedId)
            .orElseThrow {
                BusinessException(FeedErrorCode.FEED_NOT_FOUND)
            }

        if (feedLikeRepository.existsByUserAndFeed(user, feed)) {

            val feedLike = feedLikeRepository.findByUserAndFeed(user, feed)
                .orElseThrow()

            feedLikeRepository.delete(feedLike)

        } else {

            feedLikeRepository.save(
                FeedLike(user, feed)
            )
        }

        val likeCount = feedLikeRepository.countByFeed(feed).toInt()

        val isLiked = feedLikeRepository.existsByUserAndFeed(user, feed)

        return FeedLikeRes(
            likeCount,
            isLiked
        )
    }
}