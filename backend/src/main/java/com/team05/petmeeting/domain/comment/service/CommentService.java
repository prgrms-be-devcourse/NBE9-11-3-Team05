package com.team05.petmeeting.domain.comment.service;

import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.service.AnimalService;
import com.team05.petmeeting.domain.comment.dto.AnimalCommentRes;
import com.team05.petmeeting.domain.comment.dto.CommentReq;
import com.team05.petmeeting.domain.comment.dto.FeedCommentRes;
import com.team05.petmeeting.domain.comment.entity.AnimalComment;
import com.team05.petmeeting.domain.comment.entity.FeedComment;
import com.team05.petmeeting.domain.comment.errorCode.CommentErrorCode;
import com.team05.petmeeting.domain.comment.repository.AnimalCommentRepository;
import com.team05.petmeeting.domain.comment.repository.FeedCommentRepository;
import com.team05.petmeeting.domain.feed.entity.Feed;
import com.team05.petmeeting.domain.feed.service.FeedService;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final FeedCommentRepository feedCommentRepository;
    private final AnimalCommentRepository animalCommentRepository;
    private final AnimalService animalService;
    private final FeedService feedService;
    private final UserRepository userRepository;

    @Transactional
    public AnimalCommentRes createAnimalComment(Long userId, Long animalId, CommentReq commentReq) {
        User user = getUserOrThrow(userId);
        Animal animal = animalService.findByAnimalId(animalId);
        AnimalComment comment = AnimalComment.create(user, animal, commentReq.content());
        return AnimalCommentRes.from(animalCommentRepository.save(comment));
    }

    @Transactional
    public FeedCommentRes createFeedComment(Long userId, Long feedId, CommentReq commentReq) {
        User user = getUserOrThrow(userId);
        Feed feed = feedService.findByFeedId(feedId);
        FeedComment comment = FeedComment.create(user, feed, commentReq.content());
        FeedComment savedComment = feedCommentRepository.save(comment);
        return FeedCommentRes.from(savedComment);
    }

    @Transactional
    public AnimalCommentRes updateAnimalComment(Long userId, Long commentId, @Valid CommentReq commentReq) {
        AnimalComment comment = animalCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));
        validateAnimalCommentAuthor(userId, comment);
        comment.updateContent(commentReq.content());
        return AnimalCommentRes.from(animalCommentRepository.save(comment));
    }

    @Transactional
    public FeedCommentRes updateFeedComment(Long userId, Long commentId, @Valid CommentReq commentReq) {
        FeedComment comment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));
        validateFeedCommentAuthor(userId, comment);
        comment.updateContent(commentReq.content());
        return FeedCommentRes.from(feedCommentRepository.save(comment));
    }

    @Transactional
    public void deleteAnimalComment(Long userId, Long commentId) {
        AnimalComment comment = animalCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));
        validateAnimalCommentAuthor(userId, comment);
        animalCommentRepository.delete(comment);
    }

    @Transactional
    public void deleteFeedComment(Long userId, Long commentId) {
        FeedComment comment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));
        validateFeedCommentAuthor(userId, comment);
        feedCommentRepository.delete(comment);
    }

    public List<AnimalCommentRes> getAnimalComments(Long animalId) {
        return animalCommentRepository.findByAnimal_Id(animalId)
                .stream()
                .map(AnimalCommentRes::from)
                .toList();
    }

    public List<FeedCommentRes> getFeedComments(Long feedId) {
        return feedCommentRepository.findByFeed_Id(feedId)
                .stream()
                .map(FeedCommentRes::from)
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private void validateAnimalCommentAuthor(Long userId, AnimalComment comment) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(CommentErrorCode.UNAUTHORIZED);
        }
    }

    private void validateFeedCommentAuthor(Long userId, FeedComment comment) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(CommentErrorCode.UNAUTHORIZED);
        }
    }
}