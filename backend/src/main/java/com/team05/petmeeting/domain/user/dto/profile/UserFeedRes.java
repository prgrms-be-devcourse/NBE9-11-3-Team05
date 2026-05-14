package com.team05.petmeeting.domain.user.dto.profile;

import com.team05.petmeeting.domain.feed.entity.Feed;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record UserFeedRes(
        long totalFeedCount,
        List<ProfileFeedItem> feeds
) {
    public static UserFeedRes of(long totalFeedCount, List<Feed> feedList) {
        List<ProfileFeedItem> items = feedList.stream()
                .map(ProfileFeedItem::from)
                .toList();
        return new UserFeedRes(totalFeedCount, items);
    }

    public record ProfileFeedItem(
            Long feedId,
            String category,
            String title,
            String createdAt
    ) {
        public static ProfileFeedItem from(Feed feed) {
            return new ProfileFeedItem(
                    feed.getId(),
                    feed.getCategory().toString(),
                    feed.getTitle(),
                    feed.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy. MM. dd."))
            );
        }
    }
}