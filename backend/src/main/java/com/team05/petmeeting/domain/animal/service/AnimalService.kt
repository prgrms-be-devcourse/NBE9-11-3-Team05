package com.team05.petmeeting.domain.animal.service

import com.team05.petmeeting.domain.animal.dto.AnimalRes
import com.team05.petmeeting.domain.animal.entity.Animal
import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode
import com.team05.petmeeting.domain.animal.repository.AnimalRepository
import com.team05.petmeeting.global.exception.BusinessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AnimalService(
    private val animalRepository: AnimalRepository
) {


    fun findByAnimalId(animalId: Long): Animal =
        animalRepository.findById(animalId)
            .orElseThrow{ BusinessException(AnimalErrorCode.ANIMAL_NOT_FOUND) }


    fun getAnimals(region: String?, kind: String?, stateGroup: Int?, pageable: Pageable): Page<AnimalRes> {
        // 예외처리 (페이지 번호 음수)
        if (pageable.pageNumber < 0) {
            throw BusinessException(AnimalErrorCode.INVALID_PAGE_NUMBER)
        }

        // QueryDSL 레포지토리 호출 (DB에서 필터링 + 페이징 완료)
        val animalPage: Page<Animal> = animalRepository.findAnimalsWithFilter(region, kind, stateGroup, pageable)

        // 3. Entity를 DTO로 변환하여 반환
        return animalPage.map(::AnimalRes)
    }
}
