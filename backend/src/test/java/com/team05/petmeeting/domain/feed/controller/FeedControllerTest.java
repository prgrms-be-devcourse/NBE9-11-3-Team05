package com.team05.petmeeting.domain.feed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team05.petmeeting.domain.comment.dto.CommentReq;
import com.team05.petmeeting.domain.comment.dto.FeedCommentListRes;
import com.team05.petmeeting.domain.comment.dto.FeedCommentRes;
import com.team05.petmeeting.domain.comment.errorCode.CommentErrorCode;
import com.team05.petmeeting.domain.comment.service.CommentService;
import com.team05.petmeeting.domain.feed.dto.FeedReq;
import com.team05.petmeeting.domain.feed.entity.Feed;
import com.team05.petmeeting.domain.feed.enums.FeedCategory;
import com.team05.petmeeting.domain.feed.repository.FeedRepository;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import com.team05.petmeeting.global.security.test.WithCustomUser;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithCustomUser(userId = 100L)
class FeedControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private CommentService commentService;

    private Long feedId;
    private Long userId;

    // 인증 객체 만드는 헬퍼 메서드 - 매 테스트마다 반복 작성 안 해도 됨
    private UsernamePasswordAuthenticationToken auth() {
        CustomUserDetails userDetails = new CustomUserDetails(userId, List.of());
        return new UsernamePasswordAuthenticationToken(userDetails, null, List.of());
    }

    @BeforeEach
    void setUp() {
        User user = User.create("test@test.com", "테스터", "홍길동");
        user = userRepository.save(user);
        userId = user.getId();  // 실제 저장된 ID 사용

        Feed feed = new Feed(user, FeedCategory.FREE, "테스트 피드", "내용", null, null);
        feed = feedRepository.save(feed);
        feedId = feed.getId();
    }

    // ════════════════════════════════════════════════════
    //  피드 작성 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("피드 작성 성공")
    void write_success() throws Exception {
        FeedReq req = new FeedReq(FeedCategory.FREE, "새 피드 제목", "새 피드 내용", null, null);

        mvc.perform(post("/api/v1/feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(authentication(auth())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("새 피드 제목"))
                .andExpect(jsonPath("$.content").value("새 피드 내용"));
    }

    // ════════════════════════════════════════════════════
    //  피드 단건 조회 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("피드 단건 조회 성공")
    void getFeed_success() throws Exception {
        mvc.perform(get("/api/v1/feeds/{feedId}", feedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 피드"))
                .andExpect(jsonPath("$.content").value("내용"));
    }

    @Test
    @DisplayName("피드 단건 조회 실패 - 없는 피드")
    void getFeed_not_found() throws Exception {
        mvc.perform(get("/api/v1/feeds/{feedId}", 999L))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════
    //  피드 목록 조회 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("피드 목록 조회 성공")
    void getFeeds_success() throws Exception {
        mvc.perform(get("/api/v1/feeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ════════════════════════════════════════════════════
    //  피드 수정 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("피드 수정 성공")
    void modify_success() throws Exception {
        FeedReq req = new FeedReq(FeedCategory.FREE, "수정된 제목", "수정된 내용", null, null);

        mvc.perform(put("/api/v1/feeds/{feedId}", feedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    @Test
    @DisplayName("피드 수정 실패 - 없는 피드")
    void modify_not_found() throws Exception {
        FeedReq req = new FeedReq(FeedCategory.FREE, "수정된 제목", "수정된 내용", null, null);

        mvc.perform(put("/api/v1/feeds/{feedId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(authentication(auth())))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════
    //  피드 삭제 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("피드 삭제 성공")
    void delete_success() throws Exception {
        mvc.perform(delete("/api/v1/feeds/{feedId}", feedId)
                        .with(authentication(auth())))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("피드 삭제 실패 - 없는 피드")
    void delete_not_found() throws Exception {
        mvc.perform(delete("/api/v1/feeds/{feedId}", 999L)
                        .with(authentication(auth())))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════
//  추가 실패 케이스
// ════════════════════════════════════════════════════

    @Test
    @DisplayName("피드 수정 실패 - 다른 사람 피드 수정 시도 → 403")
    void modify_forbidden() throws Exception {
        // 다른 유저 생성 후 저장
        User otherUser = User.create("other@test.com", "다른유저", "김철수");
        otherUser = userRepository.save(otherUser);
        Long otherUserId = otherUser.getId();

        // 다른 유저 인증 객체 생성
        CustomUserDetails otherUserDetails = new CustomUserDetails(otherUserId, List.of());
        UsernamePasswordAuthenticationToken otherAuth =
                new UsernamePasswordAuthenticationToken(otherUserDetails, null, List.of());

        FeedReq req = new FeedReq(FeedCategory.FREE, "수정된 제목", "수정된 내용", null, null);

        // feedId는 setUp()에서 userId로 만든 피드 → otherUser가 수정 시도
        mvc.perform(put("/api/v1/feeds/{feedId}", feedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(authentication(otherAuth)))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @DisplayName("피드 삭제 실패 - 다른 사람 피드 삭제 시도 → 403")
    void delete_forbidden() throws Exception {
        // 다른 유저 생성 후 저장
        User otherUser = User.create("other@test.com", "다른유저", "김철수");
        otherUser = userRepository.save(otherUser);
        Long otherUserId = otherUser.getId();

        // 다른 유저 인증 객체 생성
        CustomUserDetails otherUserDetails = new CustomUserDetails(otherUserId, List.of());
        UsernamePasswordAuthenticationToken otherAuth =
                new UsernamePasswordAuthenticationToken(otherUserDetails, null, List.of());

        // feedId는 setUp()에서 userId로 만든 피드 → otherUser가 삭제 시도
        mvc.perform(delete("/api/v1/feeds/{feedId}", feedId)
                        .with(authentication(otherAuth)))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @DisplayName("피드 작성 실패 - 인증 없이 요청 → 401")
    @WithAnonymousUser
    void write_unauthorized() throws Exception {
        FeedReq req = new FeedReq(FeedCategory.FREE, "제목", "내용", null, null);

        // .with(authentication(auth())) 없이 요청 → 인증 없음
        mvc.perform(post("/api/v1/feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized()); // 401
    }


    // ════════════════════════════════════════════════════
    //  좋아요 토글 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("좋아요 토글 성공")
    void toggleLike_success() throws Exception {
        mvc.perform(post("/api/v1/feeds/{feedId}/likes", feedId)
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.isLiked").value(true));
    }

    // ════════════════════════════════════════════════════
    //  댓글 테스트
    // ════════════════════════════════════════════════════

    // 피드 댓글 작성 성공
    @Test
    @DisplayName("피드 댓글 생성 성공")
    void createFeedComment_success() throws Exception {
        CommentReq req = new CommentReq("테스트 댓글입니다.");
        FeedCommentRes res = new FeedCommentRes(100L, "테스터", "", 1L, "테스트 댓글입니다.", 1L, null);
        when(commentService.createFeedComment(100L, 1L, req)).thenReturn(res);

        mvc.perform(post("/api/v1/feeds/{feedId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("테스트 댓글입니다."))
                .andExpect(jsonPath("$.feedId").value(1L));
    }

    // 피드 댓글 작성 실패 - 비로그인 (401)
    @Test
    @WithAnonymousUser
    @DisplayName("피드 댓글 작성 실패 - 비로그인 (401)")
    void createFeedComment_fail_unauthorized() throws Exception {
        CommentReq req = new CommentReq("테스트 댓글입니다.");

        mvc.perform(post("/api/v1/feeds/{feedId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // 피드 댓글 작성 실패 - 빈 내용 (400)
    @Test
    @DisplayName("피드 댓글 작성 실패 - 빈 내용 (400)")
    void createFeedComment_fail_blank() throws Exception {
        CommentReq req = new CommentReq("");

        mvc.perform(post("/api/v1/feeds/{feedId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // 피드 댓글 수정 성공
    @Test
    @DisplayName("피드 댓글 수정 성공")
    void updateFeedComment_success() throws Exception {
        CommentReq req = new CommentReq("수정된 댓글입니다.");
        FeedCommentRes res = new FeedCommentRes(100L, "테스터", "", 1L, "수정된 댓글입니다.", 1L, null);
        when(commentService.updateFeedComment(100L, 1L, req)).thenReturn(res);

        mvc.perform(patch("/api/v1/feeds/{feedId}/comments/{commentId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 댓글입니다."));
    }

    // 피드 댓글 수정 실패 - 권한 없는 유저 (401)
    @Test
    @DisplayName("피드 댓글 수정 실패 - 권한 없는 유저 (401)")
    void updateFeedComment_fail_unauthorized() throws Exception {
        CommentReq req = new CommentReq("수정된 댓글입니다.");
        when(commentService.updateFeedComment(100L, 1L, req))
                .thenThrow(new BusinessException(CommentErrorCode.UNAUTHORIZED));

        mvc.perform(patch("/api/v1/feeds/{feedId}/comments/{commentId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // 피드 댓글 삭제 성공
    @Test
    @DisplayName("피드 댓글 삭제 성공")
    void deleteFeedComment_success() throws Exception {
        doNothing().when(commentService).deleteFeedComment(100L, 1L);

        mvc.perform(delete("/api/v1/feeds/{feedId}/comments/{commentId}", 1L, 1L))
                .andExpect(status().isNoContent());
    }

    // 피드 댓글 삭제 실패 - 존재하지 않는 댓글 (404)
    @Test
    @DisplayName("피드 댓글 삭제 실패 - 존재하지 않는 댓글 (404)")
    void deleteFeedComment_fail_notFound() throws Exception {
        doThrow(new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND))
                .when(commentService).deleteFeedComment(100L, 1L);

        mvc.perform(delete("/api/v1/feeds/{feedId}/comments/{commentId}", 1L, 1L))
                .andExpect(status().isNotFound());
    }

    // 피드 댓글 목록 조회 성공
    @DisplayName("피드 댓글 목록 조회 성공")
    @Test
    void getFeedComments_success() throws Exception {
        FeedCommentListRes res = new FeedCommentListRes(List.of(), 0);
        when(commentService.getFeedComments(1L)).thenReturn(List.of());

        mvc.perform(get("/api/v1/feeds/{feedId}/comments", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0));
    }

    // 피드 댓글 목록 조회 - 비로그인도 가능
    @Test
    @WithAnonymousUser
    @DisplayName("피드 댓글 목록 조회 - 비로그인도 가능")
    void getFeedComments_anonymous_success() throws Exception {
        when(commentService.getFeedComments(1L)).thenReturn(List.of());

        mvc.perform(get("/api/v1/feeds/{feedId}/comments", 1L))
                .andExpect(status().isOk());
    }
}