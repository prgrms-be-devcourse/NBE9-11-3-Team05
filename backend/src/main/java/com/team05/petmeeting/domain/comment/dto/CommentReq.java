package com.team05.petmeeting.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentReq(
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(min = 2, max = 255, message = "댓글은 2~255자 이내여야 합니다.")
        String content
) {

}