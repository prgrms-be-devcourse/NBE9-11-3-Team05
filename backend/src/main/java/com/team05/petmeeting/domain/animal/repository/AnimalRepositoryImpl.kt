package com.team05.petmeeting.domain.animal.repository

import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.team05.petmeeting.domain.animal.entity.Animal
import com.team05.petmeeting.domain.animal.entity.QAnimal.animal
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.util.StringUtils

class AnimalRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : AnimalRepositoryCustom {

    override fun findAnimalsWithFilter(
        region: String?,
        kind: String?,
        stateGroup: Int?,
        pageable: Pageable
    ): Page<Animal> {
        // 데이터처리 쿼리 (페이징)

        val content = queryFactory
            .selectFrom(animal)
            .where(
                regionStartsWith(region),
                kindEq(kind),
                stateGroupEq(stateGroup)
            )
            .offset(pageable.offset) // 페이지 시작위치
            .limit(pageable.pageSize.toLong()) // 페이지 사이즈
            .orderBy(*getOrderSpecifier(pageable.sort)) // 정렬 변환 메서드 사용 -> 코틀린은 배열을 가변 인자로 넘길 때 반드시 스프레드 연산자(*) 사용
            .fetch() // 쿼리 실행결과 -> 리스트

        // 전체 개수 조회 쿼리 (페이징에 필요)
        val total = queryFactory
            .select(animal.count())
            .from(animal)
            .where(
                regionStartsWith(region),
                kindEq(kind),
                stateGroupEq(stateGroup)
            )
            .fetchOne() // 쿼리 실행결과 -> 단일 객체

        return PageImpl(content, pageable, total ?: 0L)
    }

    // 정렬 변환 메서드: Spring Data Sort -> Querydsl OrderSpecifier로 변환

    private fun getOrderSpecifier(sort: Sort): Array<OrderSpecifier<*>> {
        // 1. QClass의 실제 alias를 가져옴 (타입 안정성 확보)
        val pathBuilder = PathBuilder(Animal::class.java, animal.metadata.name)

        // 2. map을 이용해 Sort -> OrderSpecifier로 바로 변환
        return sort.map { order ->
            val direction = if (order.isAscending) Order.ASC else Order.DESC
            val targetPath = pathBuilder.get(order.property) as Expression<out Comparable<*>>

            OrderSpecifier(direction, targetPath)
        }.toList().toTypedArray()
    }


    // null 처리하는 동적 메서드
    // contains -> LIKE '%경남%' | startsWith -> LIKE '경남%'
    // 공고번호(noticeNo)의 앞부분이 지역명으로 시작하는 API 특성을 활용하여 지역 필터링 | 경남-진주-2024-00124
    private fun regionStartsWith(region: String?): BooleanExpression? =
        if (StringUtils.hasText(region)) animal.noticeNo.startsWith(region) else null

    private fun kindEq(kind: String?): BooleanExpression? =
        if (StringUtils.hasText(kind)) animal.upKindNm.eq(kind) else null

    private fun stateGroupEq(stateGroup: Int?): BooleanExpression? =
        stateGroup?.let { animal.stateGroup.eq(it) }
}
