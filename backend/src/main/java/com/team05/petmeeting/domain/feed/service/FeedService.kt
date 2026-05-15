package com.team05.petmeeting.domain.feed.service

import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus
import com.team05.petmeeting.domain.adoption.repository.AdoptionApplicationRepository
import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode
import com.team05.petmeeting.domain.animal.repository.AnimalRepository
import com.team05.petmeeting.domain.feed.dto.AdoptedAnimalRes
import com.team05.petmeeting.domain.feed.dto.FeedListRes
import com.team05.petmeeting.domain.feed.dto.FeedReq
import com.team05.petmeeting.domain.feed.dto.FeedRes
import com.team05.petmeeting.domain.feed.entity.Feed
import com.team05.petmeeting.domain.feed.enums.FeedCategory
import com.team05.petmeeting.domain.feed.errorCode.FeedErrorCode
import com.team05.petmeeting.domain.feed.repository.FeedLikeRepository
import com.team05.petmeeting.domain.feed.repository.FeedRepository
import com.team05.petmeeting.domain.user.entity.User
import com.team05.petmeeting.global.exception.BusinessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedService(
    private val feedRepository: FeedRepository,
    private val feedLikeRepository: FeedLikeRepository,
    private val animalRepository: AnimalRepository,
    private val adoptionApplicationRepository: AdoptionApplicationRepository
) {

    @Transactional
    fun write(request: FeedReq, user: User): FeedRes {
        val animalId = request.animalId

        if (request.category == FeedCategory.ADOPTION_REVIEW && animalId == null) {
            throw BusinessException(FeedErrorCode.ANIMAL_REQUIRED)
        }

        val animal = animalId?.let {
            animalRepository.findById(it)
                .orElseThrow { BusinessException(AnimalErrorCode.ANIMAL_NOT_FOUND) }
        }

        if (request.category == FeedCategory.ADOPTION_REVIEW) {
            validateAdoptedAnimal(user, animalId!!)
        }

        val feed = Feed(
            user = user,
            category = request.category,
            title = request.title,
            content = request.content,
            imageUrl = request.imageUrl,
            animal = animal
        )

        feedRepository.save(feed)

        return FeedRes.from(feed, 0)
    }

    @Transactional
    fun modify(feedId: Long, request: FeedReq, user: User): FeedRes {
        val feed = findByFeedId(feedId)

        feed.checkModify(user)
        feed.update(request.category, request.title, request.content, request.imageUrl)

        val likeCount = feedLikeRepository.countByFeed(feed).toInt()

        return FeedRes.from(feed, likeCount)
    }

    @Transactional
    fun delete(feedId: Long, user: User) {
        val feed = findByFeedId(feedId)

        feed.checkDelete(user)
        feedRepository.delete(feed)
    }

    @Transactional(readOnly = true)
    fun getFeed(feedId: Long): FeedRes {
        val feed = findByFeedId(feedId)
        val likeCount = feedLikeRepository.countByFeed(feed).toInt()

        return FeedRes.from(feed, likeCount)
    }

    @Transactional(readOnly = true)
    fun getFeeds(pageable: Pageable, userId: Long?, category: FeedCategory?): Page<FeedListRes> {
        return feedRepository.findFeeds(pageable, userId, category)
    }

    @Transactional(readOnly = true)
    fun findByFeedId(id: Long): Feed {
        return feedRepository.findById(id)
            .orElseThrow { BusinessException(FeedErrorCode.FEED_NOT_FOUND) }
    }

    @Transactional(readOnly = true)
    fun count(): Long {
        return feedRepository.count()
    }

    @Transactional(readOnly = true)
    fun getAdoptedAnimals(userId: Long): List<AdoptedAnimalRes> {
        return adoptionApplicationRepository
            .findByUser_IdAndStatus(userId, AdoptionStatus.Approved)
            .map { AdoptedAnimalRes.from(it.getAnimal()) }
    }

    private fun validateAdoptedAnimal(user: User, animalId: Long) {
        val isApproved = adoptionApplicationRepository
            .existsByUser_IdAndAnimal_IdAndStatus(
                user.getId(),
                animalId,
                AdoptionStatus.Approved
            )

        if (!isApproved) {
            throw BusinessException(FeedErrorCode.NOT_ADOPTED_ANIMAL)
        }
    }
}
