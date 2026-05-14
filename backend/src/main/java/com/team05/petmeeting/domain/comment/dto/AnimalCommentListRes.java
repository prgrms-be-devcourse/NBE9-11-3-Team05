package com.team05.petmeeting.domain.comment.dto;

import java.util.List;

public record AnimalCommentListRes (
        List<AnimalCommentRes> comments,
        int totalCount
){
    public static AnimalCommentListRes from(List<AnimalCommentRes> comments){
        return new AnimalCommentListRes(comments, comments.size());
    }
}
