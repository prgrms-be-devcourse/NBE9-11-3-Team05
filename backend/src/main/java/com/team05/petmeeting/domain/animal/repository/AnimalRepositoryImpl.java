package com.team05.petmeeting.domain.animal.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.entity.QAnimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AnimalRepositoryImpl implements AnimalRepositoryCustom {
    private static final QAnimal animal = QAnimal.animal;

    private final JPAQueryFactory queryFactory;


    @Override
    public Page<Animal> findAnimalsWithFilter(String region, String kind, Integer stateGroup, Pageable pageable) {

        // 데이터처리 쿼리 (페이징)
        List<Animal> content = queryFactory
                .selectFrom(animal)
                .where(
                        regionStartsWith(region),
                        kindEq(kind),
                        stateGroupEq(stateGroup)
                )
                .offset(pageable.getOffset())   // 페이지 시작위치
                .limit(pageable.getPageSize())  // 페이지 사이즈
                .orderBy(getOrderSpecifier(pageable.getSort()))      // 정렬 변환 메서드 사용
                .fetch();       // 쿼리 실행결과 -> 리스트

        // 전체 개수 조회 쿼리 (페이징에 필요)
        Long total = queryFactory
                .select(animal.count())
                .from(animal)
                .where(
                        regionStartsWith(region),
                        kindEq(kind),
                        stateGroupEq(stateGroup)
                )
                .fetchOne(); // 쿼리 실행결과 -> 단일 객체

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    // 정렬 변환 메서드: Spring Data Sort -> Querydsl OrderSpecifier로 변환
    private OrderSpecifier<?>[] getOrderSpecifier(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        PathBuilder<Animal> pathBuilder = new PathBuilder<>(Animal.class, "animal");

        // Pageable에 있는 모든 정렬 조건을 순회
        sort.stream().forEach(order -> {
            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();

            orders.add(new OrderSpecifier(direction, pathBuilder.get(property)));
        });

        return orders.toArray(OrderSpecifier[]::new);
    }



    // null 처리하는 동적 메서드
    // contains -> LIKE '%경남%' | startsWith -> LIKE '경남%'
    // 공고번호(noticeNo)의 앞부분이 지역명으로 시작하는 API 특성을 활용하여 지역 필터링 | 경남-진주-2024-00124
    private BooleanExpression regionStartsWith(String region) {
        return StringUtils.hasText(region) ? animal.noticeNo.startsWith(region) : null;
    }

    private BooleanExpression kindEq(String kind) {
        return StringUtils.hasText(kind) ? animal.upKindNm.eq(kind) : null;
    }

    private BooleanExpression stateGroupEq(Integer stateGroup) {
        return stateGroup != null ? animal.stateGroup.eq(stateGroup) : null;
    }
}
