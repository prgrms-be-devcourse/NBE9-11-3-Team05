package com.team05.petmeeting.domain.feed.repository;

import com.team05.petmeeting.domain.feed.entity.Feed;
import com.team05.petmeeting.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Feed, Long>, FeedRepositoryCustom {
    Long countByUser(User user);

    List<Feed> findAllByUserOrderByCreatedAtDesc(User user);
}
