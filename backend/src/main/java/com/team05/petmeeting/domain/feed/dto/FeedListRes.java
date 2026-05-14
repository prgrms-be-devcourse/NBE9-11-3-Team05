package com.team05.petmeeting.domain.feed.dto;

import com.team05.petmeeting.domain.feed.entity.Feed;
import com.team05.petmeeting.domain.feed.enums.FeedCategory;
import java.time.LocalDateTime;

public record FeedListRes(
        Long feedId,
        Long userId,
        String profileImageUrl,
        Long animalId,
        String nickname,
        FeedCategory category,
        String title,
        String content,
        String imageUrl,
        int likeCount,
        boolean isLiked,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public FeedListRes(
            Feed feed,
            Long userId,
            String profileImageUrl,
            String nickname,
            Long animalId,
            Long likeCount,
            Long commentCount,
            boolean isLiked
    ) {
        this(
                feed.getId(),
                userId,
                profileImageUrl,
                animalId,
                nickname,
                feed.getCategory(),
                feed.getTitle(),
                feed.getContent(),
                feed.getImageUrl(),
                likeCount.intValue(),
                isLiked,
                commentCount.intValue(),
                feed.getCreatedAt(),
                feed.getUpdatedAt()
        );
    }
}
