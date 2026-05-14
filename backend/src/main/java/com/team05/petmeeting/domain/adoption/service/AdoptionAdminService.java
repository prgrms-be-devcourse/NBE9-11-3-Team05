package com.team05.petmeeting.domain.adoption.service;

import com.team05.petmeeting.domain.adoption.dto.request.AdoptionReviewRequest;
import com.team05.petmeeting.domain.adoption.dto.response.AdoptionApplyResponse;
import com.team05.petmeeting.domain.adoption.dto.response.AdoptionDetailResponse;
import com.team05.petmeeting.domain.adoption.entity.AdoptionApplication;
import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus;
import com.team05.petmeeting.domain.adoption.errorCode.AdoptionErrorCode;
import com.team05.petmeeting.domain.adoption.repository.AdoptionApplicationRepository;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.shelter.entity.Shelter;
import com.team05.petmeeting.domain.shelter.errorCode.ShelterErrorCode;
import com.team05.petmeeting.domain.shelter.repository.ShelterRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdoptionAdminService {

    private final AdoptionApplicationRepository adoptionApplicationRepository;
    private final ShelterRepository shelterRepository;

    // careRegNo 보호소의 관리자인지 확인한 뒤 해당 보호소의 입양 신청 목록만 반환한다.
    @Transactional(readOnly = true)
    public List<AdoptionApplyResponse> getManagedShelterApplications(Long userId, String careRegNo) {
        validateShelterManager(userId, careRegNo);

        return adoptionApplicationRepository.findByAnimal_Shelter_CareRegNo(careRegNo).stream()
                .map(this::toResponse)
                .toList();
    }

    // careRegNo 보호소의 관리자인지 확인한 뒤 담당 보호소 신청 상세만 반환한다.
    @Transactional(readOnly = true)
    public AdoptionDetailResponse getManagedShelterApplicationDetail(Long userId, String careRegNo, Long applicationId) {
        validateShelterManager(userId, careRegNo);

        AdoptionApplication application = getShelterApplication(careRegNo, applicationId);

        return toDetailResponse(application);
    }

    // careRegNo 보호소 관리자가 입양 신청 상태를 승인/거절/검토중으로 변경한다.
    @Transactional
    public AdoptionDetailResponse reviewApplication(
            Long userId,
            String careRegNo,
            Long applicationId,
            AdoptionReviewRequest request
    ) {
        validateShelterManager(userId, careRegNo);
        AdoptionApplication application = getShelterApplication(careRegNo, applicationId);
        AdoptionStatus status = request.getStatus();

        if (status == null) {
            throw new BusinessException(AdoptionErrorCode.INVALID_REVIEW_STATUS);
        }

        switch (status) {
            case Approved -> application.approve();
            case Rejected -> rejectApplication(application, request.getRejectionReason());
            case Processing -> application.markProcessing();
        }

        return toDetailResponse(application);
    }

    private void rejectApplication(AdoptionApplication application, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new BusinessException(AdoptionErrorCode.REJECTION_REASON_REQUIRED);
        }

        application.reject(rejectionReason);
    }

    private AdoptionApplication getShelterApplication(String careRegNo, Long applicationId) {
        AdoptionApplication application = adoptionApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(AdoptionErrorCode.APPLICATION_NOT_FOUND));

        if (!isShelterApplication(application, careRegNo)) {
            throw new BusinessException(AdoptionErrorCode.FORBIDDEN_SHELTER_APPLICATION);
        }

        return application;
    }

    // 사용자가 careRegNo 보호소의 관리자인지 확인한다.
    private void validateShelterManager(Long userId, String careRegNo) {
        Shelter shelter = shelterRepository.findById(careRegNo)
                .orElseThrow(() -> new BusinessException(ShelterErrorCode.SHELTER_NOT_FOUND));

        if (!shelter.isManagedBy(userId)) {
            throw new BusinessException(AdoptionErrorCode.UNAUTHORIZED_SHELTER);
        }
    }

    // 신청 동물의 보호소가 요청한 careRegNo 보호소인지 확인한다.
    private boolean isShelterApplication(AdoptionApplication application, String careRegNo) {
        Shelter shelter = application.getAnimal().getShelter();
        return shelter != null && shelter.getCareRegNo().equals(careRegNo);
    }

    // 관리자 목록 조회에 필요한 최소 신청 정보로 변환한다.
    private AdoptionApplyResponse toResponse(AdoptionApplication application) {
        Animal animal = application.getAnimal();

        AdoptionApplyResponse.AnimalInfo animalInfo = new AdoptionApplyResponse.AnimalInfo(
                animal.getDesertionNo(),
                animal.getKindFullNm(),
                animal.getCareNm(),
                animal.getCareOwnerNm()
        );

        return new AdoptionApplyResponse(
                application.getId(),
                application.getStatus(),
                animalInfo
        );
    }

    // 관리자 상세 조회에 필요한 신청, 연락처, 심사, 동물 정보를 함께 변환한다.
    private AdoptionDetailResponse toDetailResponse(AdoptionApplication application) {
        Animal animal = application.getAnimal();

        AdoptionDetailResponse.AnimalInfo animalInfo = new AdoptionDetailResponse.AnimalInfo(
                animal.getDesertionNo(),
                animal.getSpecialMark(),
                animal.getCareNm(),
                animal.getCareOwnerNm(),
                animal.getCareTel(),
                animal.getCareAddr()
        );

        return new AdoptionDetailResponse(
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
}
