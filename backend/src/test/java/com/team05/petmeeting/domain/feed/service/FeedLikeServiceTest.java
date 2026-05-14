package com.team05.petmeeting.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team05.petmeeting.domain.feed.dto.FeedLikeRes;
import com.team05.petmeeting.domain.feed.entity.Feed;
import com.team05.petmeeting.domain.feed.entity.FeedLike;
import com.team05.petmeeting.domain.feed.enums.FeedCategory;
import com.team05.petmeeting.domain.feed.errorCode.FeedErrorCode;
import com.team05.petmeeting.domain.feed.repository.FeedLikeRepository;
import com.team05.petmeeting.domain.feed.repository.FeedRepository;
import com.team05.petmeeting.domain.feed.service.FeedLikeService;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.global.entity.BaseEntity;
import com.team05.petmeeting.global.exception.BusinessException;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class FeedLikeServiceTest {

    @InjectMocks
    private FeedLikeService feedLikeService;

    @Mock
    private FeedLikeRepository feedLikeRepository;

    @Mock
    private FeedRepository feedRepository;

    private User user;
    private Feed feed;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        user = User.create("test@test.com", "테스터", "홍길동");
        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, 1L);

        feed = new Feed(user, FeedCategory.FREE, "제목", "내용", null, null);
        idField.set(feed, 1L);
    }

    @Test
    @DisplayName("toggleLike - 좋아요 안 눌렀을 때 좋아요 추가 성공")
    void toggleLike_add_success() {
        // given
        Long feedId = 1L;

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        // 아직 좋아요 안 누른 상태
        when(feedLikeRepository.countByFeed(feed)).thenReturn(1L);
        // countByFeed 이후 existsByUserAndFeed 재호출 → true (방금 추가했으니까)
        when(feedLikeRepository.existsByUserAndFeed(user, feed))
                .thenReturn(false)  // 첫 번째 호출 (중복 체크)
                .thenReturn(true);  // 두 번째 호출 (isLiked 세팅)

        // when
        FeedLikeRes res = feedLikeService.toggleLike(feedId, user);

        // then
        assertThat(res.likeCount()).isEqualTo(1);
        assertThat(res.isLiked()).isTrue();
        verify(feedLikeRepository).save(any(FeedLike.class)); // save 호출됐는지 확인
    }

    @Test
    @DisplayName("toggleLike - 이미 좋아요 눌렀을 때 좋아요 취소 성공")
    void toggleLike_cancel_success() {
        // given
        Long feedId = 1L;
        FeedLike existingLike = new FeedLike(user, feed);

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(feedLikeRepository.existsByUserAndFeed(user, feed))
                .thenReturn(true)   // 첫 번째 호출 (이미 눌렀는지 체크)
                .thenReturn(false); // 두 번째 호출 (isLiked 세팅)
        when(feedLikeRepository.findByUserAndFeed(user, feed)).thenReturn(Optional.of(existingLike));
        when(feedLikeRepository.countByFeed(feed)).thenReturn(0L);

        // when
        FeedLikeRes res = feedLikeService.toggleLike(feedId, user);

        // then
        assertThat(res.likeCount()).isEqualTo(0);
        assertThat(res.isLiked()).isFalse();
        verify(feedLikeRepository).delete(existingLike); // delete 호출됐는지 확인
        verify(feedLikeRepository, never()).save(any());  // save는 호출 안 됐는지 확인
    }

    @Test
    @DisplayName("toggleLike - 존재하지 않는 피드에 좋아요 시 예외 발생")
    void toggleLike_feed_not_found_throws_exception() {
        // given
        Long feedId = 999L;

        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedLikeService.toggleLike(feedId, user))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
    }
}