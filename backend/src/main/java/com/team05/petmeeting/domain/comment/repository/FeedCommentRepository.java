package com.team05.petmeeting.domain.comment.repository;

import com.team05.petmeeting.domain.comment.entity.FeedComment;
import com.team05.petmeeting.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {
    List<FeedComment> findByFeed_Id(Long feedId);

    List<FeedComment> findAllByUserOrderByCreatedAtDesc(User user);

    Long countFeedCommentByUser(User user);
}