package com.team05.petmeeting.domain.feed.repository;

import com.team05.petmeeting.domain.feed.dto.FeedListRes;
import com.team05.petmeeting.domain.feed.enums.FeedCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedRepositoryCustom {
    Page<FeedListRes> findFeeds(
            Pageable pageable,
            Long userId,
            FeedCategory category
    );
}
