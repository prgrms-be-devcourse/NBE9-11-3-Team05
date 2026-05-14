package com.team05.petmeeting.domain.naming.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.entity.QAnimal;
import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode;
import com.team05.petmeeting.domain.naming.dto.NameCandidateRes;
import com.team05.petmeeting.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.team05.petmeeting.domain.animal.entity.QAnimal.animal;
import static com.team05.petmeeting.domain.naming.entity.QAnimalNameCandidate.animalNameCandidate;
import static com.team05.petmeeting.domain.naming.entity.QNameVoteHistory.nameVoteHistory;
import static com.team05.petmeeting.domain.shelter.entity.QShelter.shelter;
import static com.team05.petmeeting.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class NamingRepositoryCustomImpl implements NamingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public NameCandidateRes getCandidates(Long animalId, Long userId) {
        // 1. 동물 정보 조회
        Animal animalEntity = queryFactory
                .selectFrom(QAnimal.animal)
                .where(QAnimal.animal.id.eq(animalId))
                .fetchOne();

        if (animalEntity == null) {
            throw new BusinessException(AnimalErrorCode.ANIMAL_NOT_FOUND);
        }

        // 2. 후보 리스트 조회 (CandidateDto 필드 순서 준수)
        List<NameCandidateRes.CandidateDto> candidateDtoList = queryFactory
                .select(Projections.constructor(NameCandidateRes.CandidateDto.class,
                        animalNameCandidate.id,
                        QAnimal.animal.id,               // 2번째: animalId
                        animalNameCandidate.proposedName,
                        animalNameCandidate.user.nickname,
                        animalNameCandidate.voteCount,
                        checkIsVoted(animalNameCandidate.id, userId)
                ))
                .from(animalNameCandidate)
                .leftJoin(animalNameCandidate.user, user)
                .where(animalNameCandidate.animal.id.eq(animalId))
                .orderBy(
                        animalNameCandidate.voteCount.desc(),
                        animalNameCandidate.createdAt.asc()
                )
                .fetch();

        return new NameCandidateRes(
                animalId,
                animalEntity.getName(),
                candidateDtoList,
                candidateDtoList.size()
        );
    }

    @Override
    public List<NameCandidateRes.CandidateDto> findAllQualifiedCandidatesByShelter(String careNm, int threshold) {
        return queryFactory
                .select(Projections.constructor(NameCandidateRes.CandidateDto.class,
                        animalNameCandidate.id,
                        QAnimal.animal.id,               // 2번째: animalId
                        animalNameCandidate.proposedName,
                        user.nickname,
                        animalNameCandidate.voteCount,
                        Expressions.asBoolean(false)
                ))
                .from(animalNameCandidate)
                .join(animalNameCandidate.animal, animal)
                .join(animal.shelter, shelter)
                .leftJoin(animalNameCandidate.user, user)
                .where(
                        shelter.careNm.eq(careNm),
                        animalNameCandidate.voteCount.goe(threshold),
                        animal.name.isNull()
                )
                .orderBy(
                        animal.id.asc(),
                        animalNameCandidate.voteCount.desc(),
                        animalNameCandidate.createdAt.asc()
                )
                .fetch();
    }

    private BooleanExpression checkIsVoted(NumberPath<Long> candidateId, Long userId) {
        if (userId == null) {
            return Expressions.asBoolean(false);
        }

        return JPAExpressions
                .selectOne()
                .from(nameVoteHistory)
                .where(nameVoteHistory.candidate.id.eq(candidateId)
                        .and(nameVoteHistory.user.id.eq(userId)))
                .exists();
    }
}