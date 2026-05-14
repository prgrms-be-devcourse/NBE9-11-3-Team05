package com.team05.petmeeting.domain.animal.controller;

import com.team05.petmeeting.domain.comment.dto.AnimalCommentListRes;
import com.team05.petmeeting.domain.comment.dto.AnimalCommentRes;
import com.team05.petmeeting.domain.comment.dto.CommentReq;
import com.team05.petmeeting.domain.comment.service.CommentService;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/animals")
@Slf4j
public class AnimalCommentController {

    private final CommentService commentService;

    @Operation(summary = "동물 댓글 조회")
    @GetMapping("/{animalId}/comments")
    public ResponseEntity<AnimalCommentListRes> getAnimalComments(
            @PathVariable Long animalId
    ) {
        log.info("=============================== 댓글 조회 호출 ================================");
        List<AnimalCommentRes> list = commentService.getAnimalComments(animalId);
        AnimalCommentListRes res = AnimalCommentListRes.from(list);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "동물 댓글 작성")
    @PostMapping("/{animalId}/comments")
    public ResponseEntity<AnimalCommentRes> createAnimalComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long animalId,
            @Valid @RequestBody CommentReq commentReq) {
        log.info("=============================== 댓글 작성 호출 ================================");
        AnimalCommentRes res = commentService.createAnimalComment(userDetails.getUserId(), animalId, commentReq);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "동물 댓글 수정")
    @PatchMapping("/{animalId}/comments/{commentId}") // 수지는 보통 Patch나 Put 사용
    public ResponseEntity<AnimalCommentRes> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long animalId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentReq commentReq) {

        AnimalCommentRes res = commentService.updateAnimalComment(userDetails.getUserId(), commentId, commentReq);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "동물 댓글 삭제")
    @DeleteMapping("/{animalId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long animalId,
            @PathVariable Long commentId) {

        commentService.deleteAnimalComment(userDetails.getUserId(), commentId);
        return ResponseEntity.noContent().build();
    }
}


