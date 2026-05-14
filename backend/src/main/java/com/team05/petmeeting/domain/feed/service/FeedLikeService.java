package com.team05.petmeeting.domain.feed.service;

import com.team05.petmeeting.domain.feed.dto.FeedLikeRes;
import com.team05.petmeeting.domain.feed.entity.Feed;
import com.team05.petmeeting.domain.feed.entity.FeedLike;
import com.team05.petmeeting.domain.feed.errorCode.FeedErrorCode;
import com.team05.petmeeting.domain.feed.repository.FeedLikeRepository;
import com.team05.petmeeting.domain.feed.repository.FeedRepository;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedLikeService {

    private final FeedLikeRepository feedLikeRepository;
    private final FeedRepository feedRepository;

    @Transactional
    public FeedLikeRes toggleLike(Long feedId, User user) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BusinessException(FeedErrorCode.FEED_NOT_FOUND));

        if (feedLikeRepository.existsByUserAndFeed(user, feed)) {
            //이미 눌렀으면 취소
            FeedLike feedLike = feedLikeRepository.findByUserAndFeed(user, feed)
                    .orElseThrow();
            feedLikeRepository.delete(feedLike);
        } else {
            //안 눌렀으면 추가
            feedLikeRepository.save(new FeedLike(user, feed));
        }
        int likeCount = (int) feedLikeRepository.countByFeed(feed);
        boolean isLiked = feedLikeRepository.existsByUserAndFeed(user, feed);

        return new FeedLikeRes(likeCount, isLiked);
    }
}
