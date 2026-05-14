package com.team05.petmeeting.domain.naming.service;

import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import com.team05.petmeeting.domain.naming.dto.BadWordAddRes;
import com.team05.petmeeting.domain.naming.dto.BadWordListRes;
import com.team05.petmeeting.domain.naming.dto.NameCandidateRes;
import com.team05.petmeeting.domain.naming.dto.NameProposalRes;
import com.team05.petmeeting.domain.naming.entity.AnimalNameCandidate;
import com.team05.petmeeting.domain.naming.entity.BadWord;
import com.team05.petmeeting.domain.naming.entity.NameVoteHistory;
import com.team05.petmeeting.domain.naming.errorCode.NamingErrorCode;
import com.team05.petmeeting.domain.naming.repository.AnimalNameCandidateRepository;
import com.team05.petmeeting.domain.naming.repository.NameVoteHistoryRepository;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NamingService {

    private final AnimalNameCandidateRepository candidateRepository;
    private final NameVoteHistoryRepository voteHistoryRepository;
    private final BadWordService badWordService; // Redis 기반 금칙어 검증 서비스
    private final UserRepository userRepository;
    private final AnimalRepository animalRepository;

    public NameCandidateRes getCandidates(Long animalId, Long userId) {
        return candidateRepository.getCandidates(animalId, userId);
    }

    public NameProposalRes proposeName(Long animalId, Long userId, String proposedName) {
        // 동물 존재 여부
        Animal animal = animalRepository.findById(animalId).orElseThrow(
                () -> new BusinessException(AnimalErrorCode.ANIMAL_NOT_FOUND));

        // 이미 종료된 동물인지 검증
        validateAnimalStatus(animal);

        // 금칙어 검증
        if (badWordService.isBadWord(proposedName)) {
            throw new BusinessException(NamingErrorCode.BAD_WORD_INCLUDED);
        }

        // 해당 동물의 이름 후보 중 동일한 이름이 있는지 확인
        Optional<AnimalNameCandidate> existingCandidate =
                candidateRepository.findByAnimalIdAndProposedName(animalId, proposedName);

        if (existingCandidate.isPresent()) {
            // 이미 존재하는 이름 -> 투표로직 리다이렉트
            Long candidateId = existingCandidate.get().getId();
            vote(candidateId, userId); // 내부 투표 로직 호출
            return new NameProposalRes(candidateId, proposedName);
        }

        User proposer = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(UserErrorCode.USER_NOT_FOUND));


        AnimalNameCandidate newCandidate = new AnimalNameCandidate(animal, proposer, proposedName);
        AnimalNameCandidate savedCandidate = candidateRepository.save(newCandidate);

        // 4. 제안자 본인의 첫 투표 처리
        vote(savedCandidate.getId(), userId);

        return new NameProposalRes(savedCandidate.getId(), proposedName);
    }

    public void vote(Long candidateId, Long userId) {
        AnimalNameCandidate candidate = candidateRepository.findById(candidateId).orElseThrow(
                () -> new BusinessException(NamingErrorCode.CANDIDATE_NOT_FOUND));

        // 투표하려는 후보가 속한 동물이 여전히 '보호중'인지 체크
        validateAnimalStatus(candidate.getAnimal());

        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        // 중복 투표 방지 -> 한 유저는 한 동물당 한번만 투표 가능
        if (voteHistoryRepository.existsByUserIdAndAnimalId(userId, candidate.getAnimal().getId())) {
            throw new BusinessException(NamingErrorCode.ALREADY_VOTED);
        }

        // 투표 이력 저장
        NameVoteHistory history = new NameVoteHistory(user, candidate.getAnimal(), candidate);
        voteHistoryRepository.save(history);

        // 후보 테이블의 투표수 증가
        candidate.addVoteCount();
    }

    @Transactional(readOnly = true)
    public List<NameCandidateRes> getAdminQualifiedList(Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (manager.getShelter() == null) {
            throw new BusinessException(NamingErrorCode.ACCESS_DENIED);
        }

        String careName = manager.getShelter().getCareNm();
        int threshold = 10;

        // 1. 해당 보호소의 10표 이상 모든 후보 조회
        List<NameCandidateRes.CandidateDto> allQualified =
                candidateRepository.findAllQualifiedCandidatesByShelter(careName, threshold);

        // 2. 동물(AnimalId)별로 그룹화하여 "가장 득표수가 높은 후보 1개"만 추출
        // (쿼리에서 voteCount.desc()로 정렬했으므로 첫 번째 값만 취하면 됨)
        Map<Long, NameCandidateRes.CandidateDto> topCandidatePerAnimal = allQualified.stream()
                .collect(Collectors.toMap(
                        dto -> dto.animalId(), // CandidateDto에 animalId 필드 추가 권장
                        dto -> dto,
                        (existing, replacement) -> existing // 이미 들어가 있으면(득표수 높은 것) 유지
                ));

        // 3. 최종 응답 리스트 생성
        return topCandidatePerAnimal.entrySet().stream()
                .map(entry -> new NameCandidateRes(
                        entry.getKey(),
                        null,
                        List.of(entry.getValue()),
                        1))
                .toList();
    }

    public void confirmName(Long candidateId, Long managerId) {
        AnimalNameCandidate candidate = candidateRepository.findById(candidateId).orElseThrow(
                () -> new BusinessException(NamingErrorCode.CANDIDATE_NOT_FOUND));

        Animal animal = candidate.getAnimal();
        // 상태 검증
        validateAnimalStatus(animal);

        // 3. 권한 검증 (팀원 구현부 연동 대비)
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        // 관리자의 careRegNo 과 동물이 속한 보호소의 careRegNo 을 비교
         if (manager.getShelter() == null ||
                 !manager.getShelter().getCareNm().equals(animal.getShelter().getCareNm())) {
             throw new BusinessException(NamingErrorCode.ACCESS_DENIED);
         }

        // 이름 확정 처리
        candidate.confirmName(); // isConfirmed = true
        candidate.getAnimal().updateName(candidate.getProposedName()); // 동물 엔터티 이름 반영
    }

    // 금칙어 관리 (BadWordService를 거쳐 DB와 Redis 동시 처리)
    public BadWordListRes getBadWords() {
        List<BadWord> badWords = badWordService.findAll();
        List<BadWordListRes.BadWordDto> dtos = badWords.stream()
                .map(bw -> new BadWordListRes.BadWordDto(bw.getId(), bw.getWord(), bw.getCreatedAt().toString()))
                .toList();
        return new BadWordListRes(dtos, dtos.size());
    }

    @Transactional
    public BadWordAddRes addBadWord(String word) {
        BadWord badWord = new BadWord(word);
        badWordService.save(badWord);
        badWordService.addBadWord(word); // Redis에도 추가
        return new BadWordAddRes(badWord.getId(), badWord.getWord(), badWord.getCreatedAt());
    }

    @Transactional
    public void deleteBadWord(Long badwordId) {
        BadWord badWord = badWordService.findById(badwordId);
        badWordService.delete(badWord);
        badWordService.deleteBadWord(badWord.getWord()); // Redis 동기화
    }

    private void validateAnimalStatus(Animal animal) {
        // 상태가 '보호중'인 경우에만 가능
        if (animal.getStateGroup() == 1) {
            throw new BusinessException(NamingErrorCode.ALREADY_COMPLETED_ANIMAL);
        }
        // 이미 이름이 확정되었는지 체크
        if (animal.getName() != null && !animal.getName().isEmpty()) {
            throw new BusinessException(NamingErrorCode.ALREADY_HAS_NAME);
        }
    }

}
