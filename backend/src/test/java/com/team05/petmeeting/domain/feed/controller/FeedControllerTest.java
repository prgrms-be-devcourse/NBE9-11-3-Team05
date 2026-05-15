package com.team05.petmeeting.domain.feed.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team05.petmeeting.domain.comment.dto.CommentReq;
import com.team05.petmeeting.domain.comment.dto.FeedCommentListRes;
import com.team05.petmeeting.domain.comment.dto.FeedCommentRes;
import com.team05.petmeeting.domain.comment.errorCode.CommentErrorCode;
import com.team05.petmeeting.domain.comment.service.CommentService;
import com.team05.petmeeting.domain.feed.dto.FeedLikeRes;
import com.team05.petmeeting.domain.feed.dto.FeedListRes;
import com.team05.petmeeting.domain.feed.dto.FeedReq;
import com.team05.petmeeting.domain.feed.dto.FeedRes;
import com.team05.petmeeting.domain.feed.enums.FeedCategory;
import com.team05.petmeeting.domain.feed.errorCode.FeedErrorCode;
import com.team05.petmeeting.domain.feed.service.FeedLikeService;
import com.team05.petmeeting.domain.feed.service.FeedService;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import com.team05.petmeeting.global.security.handler.JwtAuthenticationEntryPoint;
import com.team05.petmeeting.global.security.test.WithCustomUser;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import com.team05.petmeeting.global.security.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithCustomUser(userId = 100L)
class FeedControllerTest {

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private FeedService feedService;

    @MockitoBean
    private FeedLikeService feedLikeService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private Long feedId;
    private Long userId;

    // 인증 객체 만드는 헬퍼 메서드 - 매 테스트마다 반복 작성 안 해도 됨
    private UsernamePasswordAuthenticationToken auth() {
        CustomUserDetails userDetails = new CustomUserDetails(userId, List.of());
        return new UsernamePasswordAuthenticationToken(userDetails, null, List.of());
    }

    @BeforeEach
    void setUp() {
        userId = 100L;
        feedId = 1L;

        User user = User.create("test@test.com", "테스터", "홍길동");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    // ════════════════════════════════════════════════════
    //  피드 작성 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("피드 작성 성공")
    void write_success() throws Exception {
        FeedReq req = new FeedReq(FeedCategory.FREE, "새 피드 제목", "새 피드 내용", null, null);

        FeedRes res = new FeedRes(
                null,
                "테스터",
                1L,
                userId,
                null,
                FeedCategory.FREE,
                "새 피드 제목",
                "새 피드 내용",
                null,
                0,
                0,
                List.of(),
                null,
                null
        );
        when(feedService.write(eq(req), any(User.class))).thenReturn(res);

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
        FeedRes res = new FeedRes(
                null,
                "테스터",
                feedId,
                userId,
                null,
                FeedCategory.FREE,
                "테스트 피드",
                "내용",
                null,
                0,
                0,
                List.of(),
                null,
                null
        );
        when(feedService.getFeed(feedId)).thenReturn(res);
        mvc.perform(get("/api/v1/feeds/{feedId}", feedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 피드"))
                .andExpect(jsonPath("$.content").value("내용"));
    }

    @Test
    @DisplayName("피드 단건 조회 실패 - 없는 피드")
    void getFeed_not_found() throws Exception {
        when(feedService.getFeed(999L)).thenThrow(new BusinessException(FeedErrorCode.FEED_NOT_FOUND));
        mvc.perform(get("/api/v1/feeds/{feedId}", 999L))
                .andExpect(status().isNotFound());
    }

    // ════════════════════════════════════════════════════
    //  피드 목록 조회 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("피드 목록 조회 성공")
    void getFeeds_success() throws Exception {
        when(feedService.getFeeds(any(), any(), any())).thenReturn(new PageImpl<FeedListRes>(List.of()));
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

        FeedRes res = new FeedRes(
                null,
                "테스터",
                feedId,
                userId,
                null,
                FeedCategory.FREE,
                "수정된 제목",
                "수정된 내용",
                null,
                0,
                0,
                List.of(),
                null,
                null
        );
        when(feedService.modify(eq(feedId), eq(req), any(User.class))).thenReturn(res);

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

        when(feedService.modify(eq(999L), eq(req), any(User.class)))
                .thenThrow(new BusinessException(FeedErrorCode.FEED_NOT_FOUND));

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
        doThrow(new BusinessException(FeedErrorCode.FEED_NOT_FOUND))
                .when(feedService).delete(eq(999L), any(User.class));
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
        FeedReq req = new FeedReq(FeedCategory.FREE, "수정된 제목", "수정된 내용", null, null);
        when(feedService.modify(eq(feedId), eq(req), any(User.class)))
                .thenThrow(new BusinessException(FeedErrorCode.FORBIDDEN));
        mvc.perform(put("/api/v1/feeds/{feedId}", feedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(authentication(auth())))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @DisplayName("피드 삭제 실패 - 다른 사람 피드 삭제 시도 → 403")
    void delete_forbidden() throws Exception {
        doThrow(new BusinessException(FeedErrorCode.FORBIDDEN))
                .when(feedService).delete(eq(feedId), any(User.class));
        mvc.perform(delete("/api/v1/feeds/{feedId}", feedId)
                        .with(authentication(auth())))
                .andExpect(status().isForbidden()); // 403
    }


    // ════════════════════════════════════════════════════
    //  좋아요 토글 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("좋아요 토글 성공")
    void toggleLike_success() throws Exception {
        when(feedLikeService.toggleLike(eq(feedId), any(User.class)))
                .thenReturn(new FeedLikeRes(1, true));
        mvc.perform(post("/api/v1/feeds/{feedId}/likes", feedId)
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.isLiked").value(true));
    }


}