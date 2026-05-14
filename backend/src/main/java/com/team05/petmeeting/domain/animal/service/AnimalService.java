package com.team05.petmeeting.domain.animal.service;

import com.team05.petmeeting.domain.animal.dto.AnimalRes;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;

    public Animal findByAnimalId(Long animalId) {
        return animalRepository.findById(animalId)
                .orElseThrow(() -> new BusinessException(AnimalErrorCode.ANIMAL_NOT_FOUND));
    }

    public Page<AnimalRes> getAnimals(String region, String kind, Integer stateGroup, Pageable pageable) {
        // 예외처리 (페이지 번호 음수)
        if (pageable.getPageNumber() < 0) {
            throw new BusinessException(AnimalErrorCode.INVALID_PAGE_NUMBER);
        }

        // QueryDSL 레포지토리 호출 (DB에서 필터링 + 페이징 완료)
        Page<Animal> animalPage = animalRepository.findAnimalsWithFilter(region, kind, stateGroup, pageable);

        // 3. Entity를 DTO로 변환하여 반환
        return animalPage.map(AnimalRes::new);
    }
}
