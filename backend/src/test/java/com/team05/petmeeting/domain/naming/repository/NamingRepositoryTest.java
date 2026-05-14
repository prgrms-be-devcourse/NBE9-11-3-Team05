package com.team05.petmeeting.domain.naming.repository;

import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import com.team05.petmeeting.domain.naming.dto.NameCandidateRes;
import com.team05.petmeeting.domain.naming.entity.AnimalNameCandidate;
import com.team05.petmeeting.domain.naming.entity.NameVoteHistory;
import com.team05.petmeeting.domain.naming.repository.AnimalNameCandidateRepository;
import com.team05.petmeeting.domain.naming.repository.NameVoteHistoryRepository;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class) // QueryDSL 설정을 불러오기 위해 필수
class NamingRepositoryTest {

    @Autowired
    private AnimalNameCandidateRepository candidateRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NameVoteHistoryRepository voteHistoryRepository;

    @Autowired
    private EntityManager em;

    private User user;
    private Animal animal;

    @BeforeEach
    void setUp() {
        // 1. 테스트 유저 생성 및 저장
        user = User.create("tester@test.com", "테스터", "홍길동");
        userRepository.save(user);

        // 2. 테스트 동물 생성 및 저장
        animal = Animal.builder()
                .desertionNo("12345")
                .popfile1("test.jpg")
                .happenPlace("서울")
                .upKindNm("개")
                .kindFullNm("시바견")
                .colorCd("흰색")
                .age("2024")
                .weight("5kg")
                .processState("보호중")
                .sexCd("M")
                .specialMark("특이사항 없음")
                .careNm("행복보호소")
                .careTel("010-1234-5678")
                .careAddr("서울시")
                .stateGroup(0) // 보호중 상태
                .totalCheerCount(0) // 이 부분을 추가하세요!
                .build();
        animalRepository.save(animal);
    }

    @Test
    @DisplayName("득표수 기준 조회가 정상적으로 정렬되어 나오는지 확인")
    void getCandidates_Ordering_Success() {
        // given: 5개의 후보를 생성하고 득표수를 다르게 설정
        for (int i = 1; i <= 5; i++) {
            AnimalNameCandidate candidate = new AnimalNameCandidate(animal, user, "후보" + i);
            // i만큼 득표수 설정 (후보1: 1표, 후보2: 2표 ...)
            for (int j = 0; j < i; j++) {
                candidate.addVoteCount();
            }
            candidateRepository.save(candidate);
        }

        em.flush();
        em.clear();

        // when
        NameCandidateRes result = candidateRepository.getCandidates(animal.getId(), user.getId());

        // then: 득표순으로 정렬되었는지, 모든 후보가 나왔는지 검증
        List<NameCandidateRes.CandidateDto> list = result.getCandidateDtoList();

        assertThat(list).hasSize(5); // 모든 후보가 나오는지 확인
        assertThat(list.get(0).getProposedName()).isEqualTo("후보5"); // 최고 득표자 확인
        assertThat(list.get(0).getVoteCount()).isEqualTo(5);
        assertThat(list.get(1).getProposedName()).isEqualTo("후보4");
        assertThat(list.get(2).getProposedName()).isEqualTo("후보3");
        assertThat(list.get(3).getProposedName()).isEqualTo("후보2");
        assertThat(list.get(4).getProposedName()).isEqualTo("후보1");
    }

    @Test
    @DisplayName("로그인 유저의 투표 여부(isVoted)가 정확히 반영되는지 확인")
    void getCandidates_isVoted_Check() {
        // given
        AnimalNameCandidate candidate = new AnimalNameCandidate(animal, user, "초코");
        candidateRepository.save(candidate);

        // 유저가 해당 후보에 투표 이력을 남김
        NameVoteHistory history = new NameVoteHistory(user, animal, candidate);
        voteHistoryRepository.save(history);

        em.flush();
        em.clear();

        // when
        NameCandidateRes result = candidateRepository.getCandidates(animal.getId(), user.getId());

        // then
        assertThat(result.getCandidateDtoList().getFirst().isVoted()).isTrue(); // 투표 여부 확인
    }

    @Test
    @DisplayName("비로그인 유저 조회 시 모든 후보의 isVoted는 false여야 한다")
    void getCandidates_isVoted_False_When_Not_LoggedIn() {
        // given
        AnimalNameCandidate candidate = new AnimalNameCandidate(animal, user, "바둑이");
        candidateRepository.save(candidate);

        em.flush();
        em.clear();

        // when: userId를 null로 전달 (비로그인 상태)
        NameCandidateRes result = candidateRepository.getCandidates(animal.getId(), null);

        // then
        assertThat(result.getCandidateDtoList().getFirst().isVoted()).isFalse();
    }
}