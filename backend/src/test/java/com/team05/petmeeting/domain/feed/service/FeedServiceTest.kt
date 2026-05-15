package com.team05.petmeeting.domain.feed.service

import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus
import com.team05.petmeeting.domain.adoption.repository.AdoptionApplicationRepository
import com.team05.petmeeting.domain.animal.entity.Animal
import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode
import com.team05.petmeeting.domain.animal.repository.AnimalRepository
import com.team05.petmeeting.domain.feed.dto.FeedReq
import com.team05.petmeeting.domain.feed.entity.Feed
import com.team05.petmeeting.domain.feed.enums.FeedCategory
import com.team05.petmeeting.domain.feed.errorCode.FeedErrorCode
import com.team05.petmeeting.domain.feed.repository.FeedLikeRepository
import com.team05.petmeeting.domain.feed.repository.FeedRepository
import com.team05.petmeeting.domain.user.entity.User
import com.team05.petmeeting.domain.user.entity.User.Companion.create
import com.team05.petmeeting.global.entity.BaseEntity
import com.team05.petmeeting.global.exception.BusinessException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.Optional

internal class FeedServiceTest {
    @InjectMocks
    private lateinit var feedService: FeedService

    @Mock
    private lateinit var feedRepository: FeedRepository

    @Mock
    private lateinit var feedLikeRepository: FeedLikeRepository

    @Mock
    private lateinit var animalRepository: AnimalRepository

    @Mock
    private lateinit var adoptionApplicationRepository: AdoptionApplicationRepository

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        user = create("test@test.com", "테스터", "홍길동")
        setId(user, 1L)
    }

    @Test
    @DisplayName("write - 일반 카테고리(FREE) 피드 작성 성공")
    fun write_free_category_success() {
        val req = FeedReq(FeedCategory.FREE, "제목", "내용", null, null)
        val savedFeed = Feed(user, FeedCategory.FREE, "제목", "내용", null, null)

        Mockito.`when`(feedRepository.save(any(Feed::class.java))).thenReturn(savedFeed)

        val res = feedService.write(req, user)

        assertThat(res.title).isEqualTo("제목")
        assertThat(res.content).isEqualTo("내용")
        assertThat(res.category).isEqualTo(FeedCategory.FREE)
        verify(feedRepository).save(any(Feed::class.java))
    }

    @Test
    @DisplayName("write - ADOPTION_REVIEW + animalId 있을 때 작성 성공")
    fun write_adoption_review_with_animal_success() {
        val animalId = 1L
        val req = FeedReq(FeedCategory.ADOPTION_REVIEW, "입양후기", "내용", null, animalId)
        val animal = Animal()
        setId(animal, animalId)

        Mockito.`when`(animalRepository.findById(animalId)).thenReturn(Optional.of(animal))
        Mockito.`when`(
            adoptionApplicationRepository.existsByUser_IdAndAnimal_IdAndStatus(
                user.id,
                animalId,
                AdoptionStatus.Approved
            )
        ).thenReturn(true)
        Mockito.`when`(feedRepository.save(any(Feed::class.java)))
            .thenReturn(Feed(user, FeedCategory.ADOPTION_REVIEW, "입양후기", "내용", null, animal))

        val res = feedService.write(req, user)

        assertThat(res.title).isEqualTo("입양후기")
        assertThat(res.category).isEqualTo(FeedCategory.ADOPTION_REVIEW)
        verify(animalRepository).findById(animalId)
        verify(feedRepository).save(any(Feed::class.java))
    }

    @Test
    @DisplayName("write - ADOPTION_REVIEW인데 animalId 없으면 예외 발생")
    fun write_adoption_review_without_animal_throws_exception() {
        val req = FeedReq(FeedCategory.ADOPTION_REVIEW, "입양후기", "내용", null, null)

        assertThatThrownBy { feedService.write(req, user) }
            .isInstanceOf(BusinessException::class.java)
            .extracting { (it as BusinessException).getErrorCode() }
            .isEqualTo(FeedErrorCode.ANIMAL_REQUIRED)
    }

    @Test
    @DisplayName("write - animalId가 있는데 DB에 없으면 예외 발생")
    fun write_animal_not_found_throws_exception() {
        val animalId = 999L
        val req = FeedReq(FeedCategory.ADOPTION_REVIEW, "입양후기", "내용", null, animalId)

        Mockito.`when`(animalRepository.findById(animalId)).thenReturn(Optional.empty())

        assertThatThrownBy { feedService.write(req, user) }
            .isInstanceOf(BusinessException::class.java)
            .extracting { (it as BusinessException).getErrorCode() }
            .isEqualTo(AnimalErrorCode.ANIMAL_NOT_FOUND)
    }

    @Test
    @DisplayName("write - ADOPTION_REVIEW + 승인된 입양이 아닌 동물이면 예외 발생")
    fun write_adoption_review_not_approved_throws_exception() {
        val animalId = 1L
        val req = FeedReq(FeedCategory.ADOPTION_REVIEW, "입양후기", "내용", null, animalId)
        val animal = Animal()

        Mockito.`when`(animalRepository.findById(animalId)).thenReturn(Optional.of(animal))
        Mockito.`when`(
            adoptionApplicationRepository.existsByUser_IdAndAnimal_IdAndStatus(
                user.id,
                animalId,
                AdoptionStatus.Approved
            )
        ).thenReturn(false)

        assertThatThrownBy { feedService.write(req, user) }
            .isInstanceOf(BusinessException::class.java)
            .extracting { (it as BusinessException).getErrorCode() }
            .isEqualTo(FeedErrorCode.NOT_ADOPTED_ANIMAL)
    }

    @Test
    @DisplayName("write - ADOPTION_REVIEW + 승인된 동물이면 작성 성공")
    fun write_adoption_review_approved_success() {
        val animalId = 1L
        val req = FeedReq(FeedCategory.ADOPTION_REVIEW, "입양후기", "내용", null, animalId)
        val animal = Animal()
        setId(animal, animalId)

        Mockito.`when`(animalRepository.findById(animalId)).thenReturn(Optional.of(animal))
        Mockito.`when`(
            adoptionApplicationRepository.existsByUser_IdAndAnimal_IdAndStatus(
                user.id,
                animalId,
                AdoptionStatus.Approved
            )
        ).thenReturn(true)
        Mockito.`when`(feedRepository.save(any(Feed::class.java)))
            .thenAnswer { invocation -> invocation.getArgument(0) }

        assertThatCode { feedService.write(req, user) }
            .doesNotThrowAnyException()
    }

    @Test
    @DisplayName("modify - 본인 피드 수정 성공")
    fun modify_success() {
        val feedId = 1L
        val req = FeedReq(FeedCategory.FREE, "수정된 제목", "수정된 내용", null, null)
        val existingFeed = Feed(user, FeedCategory.FREE, "원래 제목", "원래 내용", null, null)

        Mockito.`when`(feedRepository.findById(feedId)).thenReturn(Optional.of(existingFeed))
        Mockito.`when`(feedLikeRepository.countByFeed(existingFeed)).thenReturn(0L)

        val res = feedService.modify(feedId, req, user)

        assertThat(res.title).isEqualTo("수정된 제목")
        assertThat(res.content).isEqualTo("수정된 내용")
    }

    @Test
    @DisplayName("modify - 존재하지 않는 피드 수정 시 예외 발생")
    fun modify_feed_not_found_throws_exception() {
        val feedId = 999L
        val req = FeedReq(FeedCategory.FREE, "제목", "내용", null, null)

        Mockito.`when`(feedRepository.findById(feedId)).thenReturn(Optional.empty())

        assertThatThrownBy { feedService.modify(feedId, req, user) }
            .isInstanceOf(BusinessException::class.java)
            .extracting { (it as BusinessException).getErrorCode() }
            .isEqualTo(FeedErrorCode.FEED_NOT_FOUND)
    }

    @Test
    @DisplayName("modify - 다른 사람 피드 수정 시 예외 발생")
    fun modify_other_users_feed_throws_exception() {
        val feedId = 1L
        val req = FeedReq(FeedCategory.FREE, "수정된 제목", "수정된 내용", null, null)
        val otherUser = create("other@test.com", "다른유저", "김철수")
        setId(otherUser, 2L)
        val existingFeed = Feed(otherUser, FeedCategory.FREE, "원래 제목", "원래 내용", null, null)

        Mockito.`when`(feedRepository.findById(feedId)).thenReturn(Optional.of(existingFeed))

        assertThatThrownBy { feedService.modify(feedId, req, user) }
            .isInstanceOf(BusinessException::class.java)
            .extracting { (it as BusinessException).getErrorCode() }
            .isEqualTo(FeedErrorCode.FORBIDDEN)
    }

    @Test
    @DisplayName("delete - 본인 피드 삭제 성공")
    fun delete_success() {
        val feedId = 1L
        val existingFeed = Feed(user, FeedCategory.FREE, "제목", "내용", null, null)

        Mockito.`when`(feedRepository.findById(feedId)).thenReturn(Optional.of(existingFeed))

        feedService.delete(feedId, user)

        verify(feedRepository).delete(existingFeed)
    }

    @Test
    @DisplayName("delete - 존재하지 않는 피드 삭제 시 예외 발생")
    fun delete_feed_not_found_throws_exception() {
        val feedId = 999L

        Mockito.`when`(feedRepository.findById(feedId)).thenReturn(Optional.empty())

        assertThatThrownBy { feedService.delete(feedId, user) }
            .isInstanceOf(BusinessException::class.java)
            .extracting { (it as BusinessException).getErrorCode() }
            .isEqualTo(FeedErrorCode.FEED_NOT_FOUND)
    }

    @Test
    @DisplayName("delete - 다른 사람 피드 삭제 시 예외 발생")
    fun delete_other_users_feed_throws_exception() {
        val feedId = 1L
        val otherUser = create("other@test.com", "다른유저", "김철수")
        setId(otherUser, 2L)
        val existingFeed = Feed(otherUser, FeedCategory.FREE, "제목", "내용", null, null)

        Mockito.`when`(feedRepository.findById(feedId)).thenReturn(Optional.of(existingFeed))

        assertThatThrownBy { feedService.delete(feedId, user) }
            .isInstanceOf(BusinessException::class.java)
            .extracting { (it as BusinessException).getErrorCode() }
            .isEqualTo(FeedErrorCode.FORBIDDEN)
    }

    @Test
    @DisplayName("getFeed - 피드 단건 조회 성공")
    fun getFeed_success() {
        val feedId = 1L
        val existingFeed = Feed(user, FeedCategory.FREE, "제목", "내용", null, null)

        Mockito.`when`(feedRepository.findById(feedId)).thenReturn(Optional.of(existingFeed))
        Mockito.`when`(feedLikeRepository.countByFeed(existingFeed)).thenReturn(5L)

        val res = feedService.getFeed(feedId)

        assertThat(res.title).isEqualTo("제목")
        assertThat(res.likeCount).isEqualTo(5)
    }

    @Test
    @DisplayName("getFeed - 존재하지 않는 피드 조회 시 예외 발생")
    fun getFeed_not_found_throws_exception() {
        val feedId = 999L

        Mockito.`when`(feedRepository.findById(feedId)).thenReturn(Optional.empty())

        assertThatThrownBy { feedService.getFeed(feedId) }
            .isInstanceOf(BusinessException::class.java)
            .extracting { (it as BusinessException).getErrorCode() }
            .isEqualTo(FeedErrorCode.FEED_NOT_FOUND)
    }

    private fun setId(entity: BaseEntity, id: Long) {
        val idField = BaseEntity::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(entity, id)
    }
}
