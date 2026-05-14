package com.team05.petmeeting.domain.adoption.service;

import com.team05.petmeeting.domain.adoption.dto.AdoptionApplyReq;
import com.team05.petmeeting.domain.adoption.dto.AdoptionApplyRes;
import com.team05.petmeeting.domain.adoption.dto.AdoptionDetailRes;
import com.team05.petmeeting.domain.adoption.entity.AdoptionApplication;
import com.team05.petmeeting.domain.adoption.errorCode.AdoptionErrorCode;
import com.team05.petmeeting.domain.adoption.repository.AdoptionApplicationRepository;
import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdoptionService {
    private final AdoptionApplicationRepository adoptionApplicationRepository;
    private final UserRepository userRepository;
    private final AnimalRepository animalRepository;

    // 사용자별 입양 신청 목록을 조회하고 목록 응답 DTO로 변환한다.
    public List<AdoptionApplyRes> getMyAdoptions(Long userId) {
        return adoptionApplicationRepository.findByUser_Id(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    // 목록 조회용 입양 신청 엔터티를 간단한 응답 DTO로 변환한다.
    private AdoptionApplyRes toResponse(AdoptionApplication application) {
        Animal animal = application.getAnimal();

        AdoptionApplyRes.AnimalInfo animalInfo =
                new AdoptionApplyRes.AnimalInfo(
                        animal.getDesertionNo(),
                        animal.getKindFullNm(),
                        animal.getCareNm(),
                        animal.getCareOwnerNm()
                );

        return new AdoptionApplyRes(
                application.getId(),
                application.getStatus(),
                animalInfo
        );
    }

    // 로그인한 사용자의 입양 신청 상세를 조회하고 상세 응답 DTO로 변환한다.
    public AdoptionDetailRes getApplicationDetail(Long userId, Long applicationId) {
        AdoptionApplication application = adoptionApplicationRepository
                .findByIdAndUser_Id(applicationId, userId)
                .orElseThrow(() -> new BusinessException(AdoptionErrorCode.APPLICATION_NOT_FOUND));

        return toDetailResponse(application);
    }

    // 상세 조회용 입양 신청 엔터티를 상세 응답 DTO로 변환한다.
    private AdoptionDetailRes toDetailResponse(AdoptionApplication application) {
        Animal animal = application.getAnimal();

        AdoptionDetailRes.AnimalInfo animalInfo =
                new AdoptionDetailRes.AnimalInfo(
                        animal.getDesertionNo(),
                        animal.getSpecialMark(),
                        animal.getCareNm(),
                        animal.getCareOwnerNm(),
                        animal.getCareTel(),
                        animal.getCareAddr()
                );

        return new AdoptionDetailRes(
                application.getId(),
                application.getStatus(),
                application.getApplyReason(),
                application.getCreatedAt(),
                application.getReviewedAt(),
                application.getRejectionReason(),
                application.getApplyTel(),
                animalInfo
        );
    }

    // 로그인한 사용자의 입양 신청을 저장하고 생성 결과를 응답 DTO로 반환한다.
    public AdoptionApplyRes applyApplication(Long userId, Long animalId, AdoptionApplyReq request) {
        if (adoptionApplicationRepository.existsByUser_IdAndAnimal_Id(userId, animalId)) {
            throw new BusinessException(AdoptionErrorCode.ALREADY_APPLIED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new BusinessException(AnimalErrorCode.ANIMAL_NOT_FOUND));

        AdoptionApplication application = AdoptionApplication.create(
                user,
                animal,
                request.getApplyReason(),
                request.getApplyTel()
        );

        AdoptionApplication saved = adoptionApplicationRepository.save(application);
        return toResponse(saved);
    }

    // 로그인한 사용자의 입양 신청을 조회한 뒤 본인 신청서만 삭제한다.
    public void cancelApplication(Long userId, Long applicationId) {
        AdoptionApplication application = adoptionApplicationRepository.findByIdAndUser_Id(applicationId, userId)
                .orElseThrow(() -> new BusinessException(AdoptionErrorCode.APPLICATION_NOT_FOUND));

        adoptionApplicationRepository.delete(application);
    }
}
