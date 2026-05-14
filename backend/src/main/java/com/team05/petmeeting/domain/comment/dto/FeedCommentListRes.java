package com.team05.petmeeting.domain.comment.dto;

import java.util.List;

public record FeedCommentListRes (
        List<FeedCommentRes> comments,
        int totalCount
){
    public static FeedCommentListRes from(List<FeedCommentRes> comments){
        return new FeedCommentListRes(comments, comments.size());
    }
}
