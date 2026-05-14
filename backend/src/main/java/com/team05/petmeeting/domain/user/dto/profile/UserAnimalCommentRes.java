package com.team05.petmeeting.domain.user.dto.profile;


import com.team05.petmeeting.domain.comment.entity.AnimalComment;

import java.time.LocalDateTime;
import java.util.List;

public record UserAnimalCommentRes(
        long totalCommentCount,
        List<AnimalCommentItem> comments
) {
    public static UserAnimalCommentRes of(long totalCommentCount, List<AnimalComment> commentList) {
        List<AnimalCommentItem> items = commentList.stream()
                .map(AnimalCommentItem::from)
                .toList();

        return new UserAnimalCommentRes(totalCommentCount, items);
    }

    public record AnimalCommentItem(
            long feedId,
            String desertionNo,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public static AnimalCommentItem from(AnimalComment comment) {
            return new AnimalCommentItem(
                    comment.getAnimal().getId(),
                    comment.getAnimal().getDesertionNo(),
                    comment.getContent(),
                    comment.getCreatedAt(),
                    comment.getUpdatedAt()
            );
        }
    }
}