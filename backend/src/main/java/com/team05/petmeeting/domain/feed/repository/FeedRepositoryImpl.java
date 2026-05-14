package com.team05.petmeeting.domain.feed.repository;


import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team05.petmeeting.domain.comment.entity.QFeedComment;
import com.team05.petmeeting.domain.feed.dto.FeedListRes;
import com.team05.petmeeting.domain.feed.entity.QFeed;
import com.team05.petmeeting.domain.feed.entity.QFeedLike;
import com.team05.petmeeting.domain.feed.enums.FeedCategory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<FeedListRes> findFeeds(Pageable pageable, Long userId, FeedCategory category) {

        QFeed feed = QFeed.feed;
        QFeedLike feedLike = QFeedLike.feedLike;
        QFeedComment feedComment = QFeedComment.feedComment;

        // content 조회
        List<FeedListRes> content = queryFactory
                .select(Projections.constructor(
                        FeedListRes.class,
                        feed,
                        feed.user.id,
                        feed.user.profileImageUrl,
                        feed.user.nickname,
                        feed.animal.id,
                        feedLike.countDistinct(),
                        feedComment.countDistinct(),
                        userId != null ?
                                com.querydsl.jpa.JPAExpressions
                                .selectOne()
                                .from(feedLike)
                                .where(
                                        feedLike.feed.eq(feed),
                                        feedLike.user.id.eq(userId)
                                )
                                .exists()
                                : com.querydsl.core.types.dsl.Expressions.FALSE
                ))
                .from(feed)
                .leftJoin(feed.user)
                .leftJoin(feed.animal)
                .leftJoin(feedLike).on(feedLike.feed.eq(feed))
                .leftJoin(feedComment).on(feedComment.feed.eq(feed))
                .where(categoryEq(category))
                .groupBy(feed.id)
                .orderBy(feed.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리
        Long total = queryFactory
                .select(feed.count())
                .from(feed)
                .where(categoryEq(category))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression categoryEq(FeedCategory category) {
        QFeed feed = QFeed.feed;
        return category != null ? feed.category.eq(category) : null;
    }
}
