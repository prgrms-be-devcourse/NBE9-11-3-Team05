package com.team05.petmeeting.domain.feed.repository;

import com.team05.petmeeting.domain.feed.entity.Feed;
import com.team05.petmeeting.domain.feed.entity.FeedLike;
import com.team05.petmeeting.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    //좋아요 추가(중복 체크)
    boolean existsByUserAndFeed(User user, Feed feed);

    //좋아요 취소
    Optional<FeedLike> findByUserAndFeed(User user, Feed feed);

    long countByFeed(Feed feed);
}
