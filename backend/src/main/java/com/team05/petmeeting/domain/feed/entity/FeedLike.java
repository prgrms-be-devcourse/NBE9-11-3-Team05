package com.team05.petmeeting.domain.feed.entity;

import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feed_likes")
public class FeedLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;    // FK

    @ManyToOne(fetch = FetchType.LAZY)
    private Feed feed; // FK

    public FeedLike(User user, Feed feed) {
        this.user = user;
        this.feed = feed;
    }

}
