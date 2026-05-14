package com.team05.petmeeting.domain.user.dto.profile;

import com.team05.petmeeting.domain.comment.entity.FeedComment;
import com.team05.petmeeting.domain.feed.enums.FeedCategory;

import java.time.LocalDateTime;
import java.util.List;

public record UserFeedCommentRes(
        long totalCommentCount,
        List<ProfileCommentItem> comments
) {
    public static UserFeedCommentRes of(long totalCommentCount, List<FeedComment> commentList) {
        List<ProfileCommentItem> items = commentList.stream()
                .map(ProfileCommentItem::from)
                .toList();

        return new UserFeedCommentRes(totalCommentCount, items);
    }

    public record ProfileCommentItem(
            long feedId,
            FeedCategory category,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public static ProfileCommentItem from(FeedComment comment) {
            return new ProfileCommentItem(
                    comment.getFeed().getId(),
                    comment.getFeed().getCategory(),
                    comment.getContent(),
                    comment.getCreatedAt(),
                    comment.getUpdatedAt()
            );
        }
    }
}