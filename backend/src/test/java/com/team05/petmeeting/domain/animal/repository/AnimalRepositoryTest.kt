package com.team05.petmeeting.domain.animal.repository

import com.team05.petmeeting.domain.animal.entity.Animal
import com.team05.petmeeting.domain.animal.entity.Animal.Companion.builder
import com.team05.petmeeting.global.config.JpaAuditingConfig
import com.team05.petmeeting.global.config.QueryDslConfig
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import java.util.function.Predicate

@DataJpaTest
@Import(value = [QueryDslConfig::class, JpaAuditingConfig::class])
@ActiveProfiles("test")
internal class AnimalRepositoryTest {
    @Autowired
    lateinit var animalRepository: AnimalRepository

    @BeforeEach
    fun setUp() {
        animalRepository.saveAll<Animal>(
            listOf(
                builder()
                    .desertionNo("2024001")
                    .noticeNo("경남-진주-2024-001")
                    .upKindNm("개")
                    .processState("보호중")
                    .stateGroup(0) // 필수 필드 추가!
                    .totalCheerCount(10)
                    .build(),
                builder()
                    .desertionNo("2024002")
                    .noticeNo("경남-창원-2024-002")
                    .upKindNm("개")
                    .processState("종료(입양)")
                    .stateGroup(1) // 필수 필드 추가!
                    .totalCheerCount(50)
                    .build(),
                builder()
                    .desertionNo("2024003")
                    .noticeNo("서울-강남-2024-001")
                    .upKindNm("고양이")
                    .processState("보호중")
                    .stateGroup(0) // 필수 필드 추가!
                    .totalCheerCount(30)
                    .build(),
                builder()
                    .desertionNo("2024004")
                    .noticeNo("서울-송파-2024-002")
                    .upKindNm("개")
                    .processState("보호중")
                    .stateGroup(0) // 필수 필드 추가!
                    .totalCheerCount(5)
                    .build()
            )
        )
    }

    @Test
    @DisplayName("지역 필터링(noticeNo.startsWith) 검증")
    fun filterByRegion() {
        // given
        val region = "경남" // AnimalRepositoryImpl에서 noticeNo.startsWith(region) 사용
        val pageable: Pageable = PageRequest.of(0, 10)

        // when
        val result: Page<Animal> = animalRepository.findAnimalsWithFilter(region, null, null, pageable)

        // then
        Assertions.assertThat(result.getContent()).hasSize(2)
        Assertions.assertThat(result.getContent())
            .allMatch(Predicate { a: Animal? -> a!!.noticeNo!!.startsWith("경남") })
    }

    @Test
    @DisplayName("축종 필터링(upKindNm) 검증")
    fun filterByKind() {
        // given
        val kind = "고양이"
        val pageable: Pageable = PageRequest.of(0, 10)

        // when
        val result: Page<Animal> = animalRepository.findAnimalsWithFilter(null, kind, null, pageable)

        // then
        Assertions.assertThat(result.getContent()).hasSize(1)
        Assertions.assertThat(result.getContent()[0].upKindNm).isEqualTo("고양이")
    }

    @Test
    @DisplayName("상태 그룹 필터링(stateGroup) 검증")
    fun filterByStateGroup() {
        // given
        val stateGroup = 1 // 종료 상태
        val pageable: Pageable = PageRequest.of(0, 10)

        // when
        val result: Page<Animal> = animalRepository.findAnimalsWithFilter(null, null, stateGroup, pageable)

        // then
        Assertions.assertThat(result.getContent()).hasSize(1)
        Assertions.assertThat(result.getContent()[0].processState).contains("종료")
    }

    @Test
    @DisplayName("페이징 및 정렬(응원수 기준) 검증")
    fun pagingAndSorting() {
        // given
        // totalCheerCount 내림차순 정렬, 페이지당 2개 조회
        val pageable: Pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "totalCheerCount"))

        // when
        val result: Page<Animal> = animalRepository.findAnimalsWithFilter(null, null, null, pageable)

        // then
        Assertions.assertThat(result.getContent()).hasSize(2)
        // 1위: 경남 개(50), 2위: 서울 고양이(30)
        Assertions.assertThat(result.getContent()[0].totalCheerCount).isEqualTo(50)
        Assertions.assertThat(result.getContent()[1].totalCheerCount).isEqualTo(30)
        Assertions.assertThat(result.totalElements).isEqualTo(4)
    }
}