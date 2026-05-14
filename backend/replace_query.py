import sys

path = "src/main/java/com/team05/petmeeting/domain/naming/repository/NamingRepositoryCustomImpl.java"
with open(path, "r") as f:
    content = f.read()

import_statement = "import static com.team05.petmeeting.domain.user.entity.QUser.user;"
if "import static com.team05.petmeeting.domain.shelter.entity.QShelter.shelter;" not in content:
    content = content.replace(import_statement, import_statement + "\nimport static com.team05.petmeeting.domain.shelter.entity.QShelter.shelter;")

old_code = """    public Optional<NameCandidateRes.CandidateDto> getTopQualifiedCandidate(Long animalId, String careRegNo, int threshold) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(NameCandidateRes.CandidateDto.class,
                        animalNameCandidate.id,
                        animalNameCandidate.proposedName,
                        animalNameCandidate.user.nickname,
                        animalNameCandidate.voteCount,
                        Expressions.asBoolean(false)
                ))
                .from(animalNameCandidate)
                .join(animalNameCandidate.animal, animal) // 동물 정보 조인
                .where(
                        animal.id.eq(animalId),
                        animal.shelter.careRegNo.eq(careRegNo), // [추가] 해당 보호소 동물인지 확인
                        animalNameCandidate.voteCount.goe(threshold)
                )
                .orderBy(
                        animalNameCandidate.voteCount.desc(),
                        animalNameCandidate.createdAt.asc()
                )
                .limit(1)
                .fetchOne());
    }"""

new_code = """    public Optional<NameCandidateRes.CandidateDto> getTopQualifiedCandidate(Long animalId, String careRegNo, int threshold) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(NameCandidateRes.CandidateDto.class,
                        animalNameCandidate.id,
                        animalNameCandidate.proposedName,
                        user.nickname,
                        animalNameCandidate.voteCount,
                        Expressions.asBoolean(false)
                ))
                .from(animalNameCandidate)
                .join(animalNameCandidate.animal, animal) // 동물 정보 조인
                .join(animal.shelter, shelter)
                .leftJoin(animalNameCandidate.user, user)
                .where(
                        animal.id.eq(animalId),
                        shelter.careRegNo.eq(careRegNo), // [추가] 해당 보호소 동물인지 확인
                        animalNameCandidate.voteCount.goe(threshold)
                )
                .orderBy(
                        animalNameCandidate.voteCount.desc(),
                        animalNameCandidate.createdAt.asc()
                )
                .limit(1)
                .fetchOne());
    }"""

content = content.replace(old_code, new_code)

with open(path, "w") as f:
    f.write(content)
