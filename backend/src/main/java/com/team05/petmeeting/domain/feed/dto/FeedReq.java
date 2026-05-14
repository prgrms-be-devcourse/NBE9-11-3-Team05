package com.team05.petmeeting.domain.feed.dto;

import com.team05.petmeeting.domain.feed.enums.FeedCategory;

public record FeedReq(
        FeedCategory category,
        String title,
        String content,
        String imageUrl,
        Long animalId
) {
}
