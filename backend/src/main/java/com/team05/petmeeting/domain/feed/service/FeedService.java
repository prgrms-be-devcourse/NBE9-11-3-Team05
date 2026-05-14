package com.team05.petmeeting.domain.feed.service;

import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus;
import com.team05.petmeeting.domain.adoption.repository.AdoptionApplicationRepository;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import com.team05.petmeeting.domain.feed.dto.AdoptedAnimalRes;
import com.team05.petmeeting.domain.feed.dto.FeedListRes;
import com.team05.petmeeting.domain.feed.dto.FeedReq;
import com.team05.petmeeting.domain.feed.dto.FeedRes;
import com.team05.petmeeting.domain.feed.entity.Feed;
import com.team05.petmeeting.domain.feed.enums.FeedCategory;
import com.team05.petmeeting.domain.feed.errorCode.FeedErrorCode;
import com.team05.petmeeting.domain.feed.repository.FeedLikeRepository;
import com.team05.petmeeting.domain.feed.repository.FeedRepository;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final AnimalRepository animalRepository;
    private final AdoptionApplicationRepository adoptionApplicationRepository;

    @Transactional
    public FeedRes write(FeedReq request, User user) {
        // 유효성 검증
        if (request.category() == FeedCategory.ADOPTION_REVIEW && request.animalId() == null) {
            throw new BusinessException(FeedErrorCode.ANIMAL_REQUIRED);
        }
        Animal animal = null;
        if (request.animalId() != null) {
            animal = animalRepository.findById(request.animalId())
                    .orElseThrow(() -> new BusinessException(AnimalErrorCode.ANIMAL_NOT_FOUND));

            // 입양후기 카테고리면 승인된 입양인지 검증
            if (request.category() == FeedCategory.ADOPTION_REVIEW) {
                boolean isApproved = adoptionApplicationRepository
                        .findByUser_IdAndStatus(user.getId(), AdoptionStatus.Approved)
                        .stream()
                        .anyMatch(app -> app.getAnimal().getId().equals(request.animalId()));

                if (!isApproved) {
                    throw new BusinessException(FeedErrorCode.NOT_ADOPTED_ANIMAL);
                }
            }
        }

        Feed feed = new Feed(user, request.category(), request.title(), request.content(), request.imageUrl(), animal);
        feedRepository.save(feed);
        return new FeedRes(feed, 0);
    }

    @Transactional
    public FeedRes modify(Long feedId, FeedReq request, User user) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BusinessException(FeedErrorCode.FEED_NOT_FOUND));
        feed.checkModify(user);
        feed.update(request.category(), request.title(), request.content(), request.imageUrl());
        int likeCount = (int) feedLikeRepository.countByFeed(feed);
        return new FeedRes(feed, likeCount);
    }

    @Transactional
    public void delete(Long feedId, User user) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BusinessException(FeedErrorCode.FEED_NOT_FOUND));
        feed.checkDelete(user);
        feedRepository.delete(feed);
    }

    public FeedRes getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BusinessException(FeedErrorCode.FEED_NOT_FOUND));
        int likeCount = (int) feedLikeRepository.countByFeed(feed);
        return new FeedRes(feed, likeCount);
    }

    public Page<FeedListRes> getFeeds(Pageable pageable, Long userId, FeedCategory category) {
        return feedRepository.findFeeds(pageable, userId, category);
    }

    public Feed findByFeedId(Long id) {
        return feedRepository.findById(id).orElseThrow(() -> new BusinessException(FeedErrorCode.FEED_NOT_FOUND));
    }

    public long count() {
        return feedRepository.count();
    }


    public List<AdoptedAnimalRes> getAdoptedAnimals(Long userId) {
        return adoptionApplicationRepository
                .findByUser_IdAndStatus(userId, AdoptionStatus.Approved)
                .stream()
                .map(app -> AdoptedAnimalRes.from(app.getAnimal()))
                .toList();
    }
}
