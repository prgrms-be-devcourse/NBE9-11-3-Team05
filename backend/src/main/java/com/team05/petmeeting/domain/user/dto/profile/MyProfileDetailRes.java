package com.team05.petmeeting.domain.user.dto.profile;

import lombok.Builder;

@Builder
public record MyProfileDetailRes(
        Long feedCount,
        Long cheerCount,
        Long feedCommentCount,
        Long animalCommentCount
) {
    public static MyProfileDetailRes of(Long feedCount, Long cheerCount, Long feedCommentCount, Long animalCommentCount) {
        return new MyProfileDetailRes(feedCount, cheerCount, feedCommentCount, animalCommentCount);
    }
}
