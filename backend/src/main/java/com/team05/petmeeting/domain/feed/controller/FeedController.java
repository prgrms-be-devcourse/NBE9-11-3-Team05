package com.team05.petmeeting.domain.feed.controller;

import com.team05.petmeeting.domain.comment.dto.CommentReq;
import com.team05.petmeeting.domain.comment.dto.FeedCommentListRes;
import com.team05.petmeeting.domain.comment.dto.FeedCommentRes;
import com.team05.petmeeting.domain.comment.service.CommentService;
import com.team05.petmeeting.domain.feed.dto.*;
import com.team05.petmeeting.domain.feed.enums.FeedCategory;
import com.team05.petmeeting.domain.feed.service.FeedLikeService;
import com.team05.petmeeting.domain.feed.service.FeedService;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feeds")
@Slf4j
public class FeedController {

    private final CommentService commentService;
    private final FeedService feedService;
    private final UserRepository userRepository;
    private final FeedLikeService feedLikeService;

    // 댓글 작성
    @Operation(summary = "피드 댓글 작성")
    @PostMapping("/{feedId}/comments")
    public ResponseEntity<FeedCommentRes> createFeedComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long feedId,
            @Valid @RequestBody CommentReq commentReq) {
        log.info("=============== 피드 댓글 작성 =================");
        FeedCommentRes res = commentService.createFeedComment(userDetails.getUserId(), feedId, commentReq);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "피드 댓글 수정")
    @PatchMapping("/{feedId}/comments/{commentId}")
    public ResponseEntity<FeedCommentRes> updateComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @PathVariable Long feedId,
                                                        @PathVariable Long commentId,
                                                        @Valid @RequestBody CommentReq commentReq) {
        FeedCommentRes res = commentService.updateFeedComment(userDetails.getUserId(), commentId, commentReq);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "피드 댓글 삭제")
    @DeleteMapping("/{feedId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long feedId,
            @PathVariable Long commentId) {
        commentService.deleteFeedComment(userDetails.getUserId(), commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "피드 댓글 목록 조회")
    @GetMapping("/{feedId}/comments")
    public ResponseEntity<FeedCommentListRes> getFeedComments(
            @PathVariable Long feedId
    ) {
        log.info("=============== 피드 댓글 조회 =================");
        List<FeedCommentRes> list = commentService.getFeedComments(feedId);
        FeedCommentListRes res = FeedCommentListRes.from(list);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "피드 글 작성")
    @PostMapping
    public ResponseEntity<FeedRes> write(
            @RequestBody FeedReq request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userRepository.findById(userDetails.getUserId()).orElseThrow();
        FeedRes res = feedService.write(request, user);
        return ResponseEntity.status(201).body(res);
    }

    @Operation(summary = "피드 글 수정")
    @PutMapping("/{feedId}")
    public ResponseEntity<FeedRes> modify(
            @PathVariable Long feedId,
            @RequestBody FeedReq request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userRepository.findById(userDetails.getUserId()).orElseThrow();
        FeedRes res = feedService.modify(feedId, request, user);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "피드 글 삭제")
    @DeleteMapping("/{feedId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long feedId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        User user = userRepository.findById(userDetails.getUserId()).orElseThrow();
        feedService.delete(feedId, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "피드 상세 조회")
    @GetMapping("/{feedId}")
    public ResponseEntity<FeedRes> getFeed(
            @PathVariable Long feedId
    ) {
        FeedRes res = feedService.getFeed(feedId);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "피드 목록 조회")
    @GetMapping
    public ResponseEntity<Page<FeedListRes>> getFeeds(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails,  // nullable
            @RequestParam(required = false) FeedCategory category
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        Page<FeedListRes> feeds = feedService.getFeeds(pageable, userId, category);
        return ResponseEntity.ok(feeds);
    }

    @Operation(summary = "피드 좋아요 토글")
    @PostMapping("/{feedId}/likes")
    public ResponseEntity<FeedLikeRes> toggleLike(
            @PathVariable Long feedId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userRepository.findById(userDetails.getUserId()).orElseThrow();
        FeedLikeRes res = feedLikeService.toggleLike(feedId, user);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "입양 후기 작성용 동물 목록 조회")
    @GetMapping("/adoptable-animals")
    public ResponseEntity<List<AdoptedAnimalRes>> getAdoptedAnimals(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<AdoptedAnimalRes> res = feedService.getAdoptedAnimals(userDetails.getUserId());
        return ResponseEntity.ok(res);
    }
}
