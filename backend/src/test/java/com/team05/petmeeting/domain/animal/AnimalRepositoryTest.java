package com.team05.petmeeting.domain.animal;

import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AnimalRepositoryTest {

    @Autowired
    private AnimalRepository animalRepository;

    @BeforeEach
    void setUp() {
        animalRepository.saveAll(List.of(
                Animal.builder()
                        .desertionNo("2024001")
                        .noticeNo("경남-진주-2024-001")
                        .upKindNm("개")
                        .processState("보호중")
                        .stateGroup(0) // 필수 필드 추가!
                        .totalCheerCount(10)
                        .build(),
                Animal.builder()
                        .desertionNo("2024002")
                        .noticeNo("경남-창원-2024-002")
                        .upKindNm("개")
                        .processState("종료(입양)")
                        .stateGroup(1) // 필수 필드 추가!
                        .totalCheerCount(50)
                        .build(),
                Animal.builder()
                        .desertionNo("2024003")
                        .noticeNo("서울-강남-2024-001")
                        .upKindNm("고양이")
                        .processState("보호중")
                        .stateGroup(0) // 필수 필드 추가!
                        .totalCheerCount(30)
                        .build(),
                Animal.builder()
                        .desertionNo("2024004")
                        .noticeNo("서울-송파-2024-002")
                        .upKindNm("개")
                        .processState("보호중")
                        .stateGroup(0) // 필수 필드 추가!
                        .totalCheerCount(5)
                        .build()
        ));
    }

    @Test
    @DisplayName("지역 필터링(noticeNo.startsWith) 검증")
    void filterByRegion() {
        // given
        String region = "경남"; // AnimalRepositoryImpl에서 noticeNo.startsWith(region) 사용
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Animal> result = animalRepository.findAnimalsWithFilter(region, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(a -> a.getNoticeNo().startsWith("경남"));
    }

    @Test
    @DisplayName("축종 필터링(upKindNm) 검증")
    void filterByKind() {
        // given
        String kind = "고양이";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Animal> result = animalRepository.findAnimalsWithFilter(null, kind, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUpKindNm()).isEqualTo("고양이");
    }

    @Test
    @DisplayName("상태 그룹 필터링(stateGroup) 검증")
    void filterByStateGroup() {
        // given
        Integer stateGroup = 1; // 종료 상태
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Animal> result = animalRepository.findAnimalsWithFilter(null, null, stateGroup, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProcessState()).contains("종료");
    }

    @Test
    @DisplayName("페이징 및 정렬(응원수 기준) 검증")
    void pagingAndSorting() {
        // given
        // totalCheerCount 내림차순 정렬, 페이지당 2개 조회
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "totalCheerCount"));

        // when
        Page<Animal> result = animalRepository.findAnimalsWithFilter(null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        // 1위: 경남 개(50), 2위: 서울 고양이(30)
        assertThat(result.getContent().get(0).getTotalCheerCount()).isEqualTo(50);
        assertThat(result.getContent().get(1).getTotalCheerCount()).isEqualTo(30);
        assertThat(result.getTotalElements()).isEqualTo(4);
    }
}