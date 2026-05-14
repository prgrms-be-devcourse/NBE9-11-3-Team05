package com.team05.petmeeting.domain.feed.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import com.team05.petmeeting.domain.feed.dto.FeedReq;
import com.team05.petmeeting.domain.feed.dto.FeedRes;
import com.team05.petmeeting.domain.feed.entity.Feed;
import com.team05.petmeeting.domain.feed.enums.FeedCategory;
import com.team05.petmeeting.domain.feed.errorCode.FeedErrorCode;
import com.team05.petmeeting.domain.feed.repository.FeedLikeRepository;
import com.team05.petmeeting.domain.feed.repository.FeedRepository;
import com.team05.petmeeting.domain.feed.service.FeedService;
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
import com.team05.petmeeting.domain.adoption.entity.AdoptionApplication;
import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus;
import com.team05.petmeeting.domain.adoption.repository.AdoptionApplicationRepository;
import java.util.List;
import static org.mockito.Mockito.mock;

class FeedServiceTest {

    @InjectMocks
    private FeedService feedService;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedLikeRepository feedLikeRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private AdoptionApplicationRepository adoptionApplicationRepository;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        user = User.create("test@test.com", "테스터", "홍길동");

        // DB 없이 ID 강제 주입
        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, 1L);
    }

    // ════════════════════════════════════════════════════
    //  write() 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("write - 일반 카테고리(FREE) 피드 작성 성공")
    void write_free_category_success() {
        // given
        FeedReq req = new FeedReq(FeedCategory.FREE, "제목", "내용", null, null);
        Feed savedFeed = new Feed(user, FeedCategory.FREE, "제목", "내용", null, null);

        when(feedRepository.save(any(Feed.class))).thenReturn(savedFeed);

        // when
        FeedRes res = feedService.write(req, user);

        // then
        assertThat(res.title()).isEqualTo("제목");
        assertThat(res.content()).isEqualTo("내용");
        assertThat(res.category()).isEqualTo(FeedCategory.FREE);
        verify(feedRepository).save(any(Feed.class));
    }

    @Test
    @DisplayName("write - ADOPTION_REVIEW + animalId 있을 때 작성 성공")
    void write_adoption_review_with_animal_success() throws Exception {
        // given
        Long animalId = 1L;
        FeedReq req = new FeedReq(FeedCategory.ADOPTION_REVIEW, "입양후기", "내용", null, animalId);
        Animal animal = new Animal();

        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(animal, animalId);

        AdoptionApplication app = mock(AdoptionApplication.class);
        when(app.getAnimal()).thenReturn(animal);

        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));
        when(adoptionApplicationRepository.findByUser_IdAndStatus(1L, AdoptionStatus.Approved))
                .thenReturn(List.of(app));
        when(feedRepository.save(any(Feed.class))).thenReturn(new Feed(user, FeedCategory.ADOPTION_REVIEW, "입양후기", "내용", null, animal));

        // when
        FeedRes res = feedService.write(req, user);

        // then
        assertThat(res.title()).isEqualTo("입양후기");
        assertThat(res.category()).isEqualTo(FeedCategory.ADOPTION_REVIEW);
        verify(animalRepository).findById(animalId);
        verify(feedRepository).save(any(Feed.class));
    }

    @Test
    @DisplayName("write - ADOPTION_REVIEW인데 animalId 없으면 예외 발생")
    void write_adoption_review_without_animal_throws_exception() {
        // given
        FeedReq req = new FeedReq(FeedCategory.ADOPTION_REVIEW, "입양후기", "내용", null, null);

        // when & then
        assertThatThrownBy(() -> feedService.write(req, user))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(FeedErrorCode.ANIMAL_REQUIRED);
    }

    @Test
    @DisplayName("write - animalId가 있는데 DB에 없으면 예외 발생")
    void write_animal_not_found_throws_exception() {
        // given
        Long animalId = 999L;
        FeedReq req = new FeedReq(FeedCategory.ADOPTION_REVIEW, "입양후기", "내용", null, animalId);

        when(animalRepository.findById(animalId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedService.write(req, user))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AnimalErrorCode.ANIMAL_NOT_FOUND);
    }

    @Test
    @DisplayName("write - ADOPTION_REVIEW + 승인된 입양이 아닌 동물이면 예외 발생")
    void write_adoption_review_not_approved_throws_exception() {
        // given
        Long animalId = 1L;
        FeedReq req = new FeedReq(FeedCategory.ADOPTION_REVIEW, "입양후기", "내용", null, animalId);
        Animal animal = new Animal();

        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));
        when(adoptionApplicationRepository.findByUser_IdAndStatus(1L, AdoptionStatus.Approved))
                .thenReturn(List.of()); // 승인된 입양 없음

        // when & then
        assertThatThrownBy(() -> feedService.write(req, user))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(FeedErrorCode.NOT_ADOPTED_ANIMAL);
    }

    @Test
    @DisplayName("write - ADOPTION_REVIEW + 승인된 동물이면 작성 성공")
    void write_adoption_review_approved_success() throws Exception {
        // given
        Long animalId = 1L;
        FeedReq req = new FeedReq(FeedCategory.ADOPTION_REVIEW, "입양후기", "내용", null, animalId);
        Animal animal = new Animal();

        AdoptionApplication app = mock(AdoptionApplication.class);
        when(app.getAnimal()).thenReturn(animal);

        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(animal, animalId);

        when(animalRepository.findById(animalId)).thenReturn(Optional.of(animal));
        when(adoptionApplicationRepository.findByUser_IdAndStatus(1L, AdoptionStatus.Approved))
                .thenReturn(List.of(app));
        when(feedRepository.save(any(Feed.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when & then
        assertThatCode(() -> feedService.write(req, user))
                .doesNotThrowAnyException();
    }

    // ════════════════════════════════════════════════════
    //  modify() 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("modify - 본인 피드 수정 성공")
    void modify_success() {
        // given
        Long feedId = 1L;
        FeedReq req = new FeedReq(FeedCategory.FREE, "수정된 제목", "수정된 내용", null, null);
        Feed existingFeed = new Feed(user, FeedCategory.FREE, "원래 제목", "원래 내용", null, null);

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(existingFeed));
        when(feedLikeRepository.countByFeed(existingFeed)).thenReturn(0L);

        // when
        FeedRes res = feedService.modify(feedId, req, user);

        // then
        assertThat(res.title()).isEqualTo("수정된 제목");
        assertThat(res.content()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("modify - 존재하지 않는 피드 수정 시 예외 발생")
    void modify_feed_not_found_throws_exception() {
        // given
        Long feedId = 999L;
        FeedReq req = new FeedReq(FeedCategory.FREE, "제목", "내용", null, null);

        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedService.modify(feedId, req, user))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("modify - 다른 사람 피드 수정 시 예외 발생")
    void modify_other_users_feed_throws_exception() throws Exception {
        // given
        Long feedId = 1L;
        FeedReq req = new FeedReq(FeedCategory.FREE, "수정된 제목", "수정된 내용", null, null);

        User otherUser = User.create("other@test.com", "다른유저", "김철수");
        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(otherUser, 2L);  // user는 1L, otherUser는 2L

        Feed existingFeed = new Feed(otherUser, FeedCategory.FREE, "원래 제목", "원래 내용", null, null);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(existingFeed));

        // when & then
        assertThatThrownBy(() -> feedService.modify(feedId, req, user))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(FeedErrorCode.FORBIDDEN);
    }

    // ════════════════════════════════════════════════════
    //  delete() 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("delete - 본인 피드 삭제 성공")
    void delete_success() {
        // given
        Long feedId = 1L;
        Feed existingFeed = new Feed(user, FeedCategory.FREE, "제목", "내용", null, null);

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(existingFeed));

        // when
        feedService.delete(feedId, user);

        // then
        verify(feedRepository).delete(existingFeed);
    }

    @Test
    @DisplayName("delete - 존재하지 않는 피드 삭제 시 예외 발생")
    void delete_feed_not_found_throws_exception() {
        // given
        Long feedId = 999L;

        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedService.delete(feedId, user))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("delete - 다른 사람 피드 삭제 시 예외 발생")
    void delete_other_users_feed_throws_exception() throws Exception {
        // given
        Long feedId = 1L;

        User otherUser = User.create("other@test.com", "다른유저", "김철수");
        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(otherUser, 2L);

        Feed existingFeed = new Feed(otherUser, FeedCategory.FREE, "제목", "내용", null, null);
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(existingFeed));

        // when & then
        assertThatThrownBy(() -> feedService.delete(feedId, user))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(FeedErrorCode.FORBIDDEN);
    }

    // ════════════════════════════════════════════════════
    //  getFeed() 테스트
    // ════════════════════════════════════════════════════

    @Test
    @DisplayName("getFeed - 피드 단건 조회 성공")
    void getFeed_success() {
        // given
        Long feedId = 1L;
        Feed existingFeed = new Feed(user, FeedCategory.FREE, "제목", "내용", null, null);

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(existingFeed));
        when(feedLikeRepository.countByFeed(existingFeed)).thenReturn(5L);

        // when
        FeedRes res = feedService.getFeed(feedId);

        // then
        assertThat(res.title()).isEqualTo("제목");
        assertThat(res.likeCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("getFeed - 존재하지 않는 피드 조회 시 예외 발생")
    void getFeed_not_found_throws_exception() {
        // given
        Long feedId = 999L;

        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> feedService.getFeed(feedId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(FeedErrorCode.FEED_NOT_FOUND);
    }
}