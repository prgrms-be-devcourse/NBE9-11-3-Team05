package com.team05.petmeeting.domain.adoption.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team05.petmeeting.domain.adoption.dto.AdoptionReviewReq;
import com.team05.petmeeting.domain.adoption.dto.AdoptionApplyRes;
import com.team05.petmeeting.domain.adoption.dto.AdoptionDetailRes;
import com.team05.petmeeting.domain.adoption.entity.AdoptionApplication;
import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus;
import com.team05.petmeeting.domain.adoption.errorCode.AdoptionErrorCode;
import com.team05.petmeeting.domain.adoption.repository.AdoptionApplicationRepository;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.shelter.dto.ShelterCommand;
import com.team05.petmeeting.domain.shelter.entity.Shelter;
import com.team05.petmeeting.domain.shelter.repository.ShelterRepository;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.global.entity.BaseEntity;
import com.team05.petmeeting.global.exception.BusinessException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdoptionAdminServiceTest {

    @InjectMocks
    AdoptionAdminService adoptionAdminService;

    @Mock
    AdoptionApplicationRepository adoptionApplicationRepository;

    @Mock
    ShelterRepository shelterRepository;

    @Test
    @DisplayName("담당 보호소의 입양 신청 목록만 조회한다")
    void getManagedShelterApplications() throws Exception {
        // given: 담당 보호소 신청과 다른 보호소 신청이 함께 존재한다.
        User manager = createUser(1L, "manager@test.com");
        User otherManager = createUser(2L, "other-manager@test.com");
        User applicant = createUser(3L, "applicant@test.com");

        AdoptionApplication managedApplication = createApplication(
                1L,
                applicant,
                createAnimal("A-001", "담당보호소", createShelter("S-001", "담당보호소", manager))
        );
        AdoptionApplication otherApplication = createApplication(
                2L,
                applicant,
                createAnimal("A-002", "다른보호소", createShelter("S-002", "다른보호소", otherManager))
        );
        when(shelterRepository.findById("S-001"))
                .thenReturn(Optional.of(managedApplication.getAnimal().getShelter()));
        when(adoptionApplicationRepository.findByAnimal_Shelter_CareRegNo("S-001"))
                .thenReturn(List.of(managedApplication));

        // when
        List<AdoptionApplyRes> responses =
                adoptionAdminService.getManagedShelterApplications(manager.getId(), "S-001");

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getApplicationId()).isEqualTo(managedApplication.getId());
        assertThat(responses.get(0).getStatus()).isEqualTo(AdoptionStatus.Processing);
        assertThat(responses.get(0).getAnimalInfo().getCareNm()).isEqualTo("담당보호소");
    }

    @Test
    @DisplayName("담당 보호소의 입양 신청 상세를 조회한다")
    void getManagedShelterApplicationDetail() throws Exception {
        // given: 관리자가 담당하는 보호소의 입양 신청이 존재한다.
        User manager = createUser(1L, "manager@test.com");
        User applicant = createUser(2L, "applicant@test.com");
        Animal animal = createAnimal("A-001", "담당보호소", createShelter("S-001", "담당보호소", manager));
        AdoptionApplication application = createApplication(1L, applicant, animal);
        when(shelterRepository.findById("S-001"))
                .thenReturn(Optional.of(animal.getShelter()));
        when(adoptionApplicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));

        // when
        AdoptionDetailRes response =
                adoptionAdminService.getManagedShelterApplicationDetail(manager.getId(), "S-001", application.getId());

        // then
        assertThat(response.getApplicationId()).isEqualTo(application.getId());
        assertThat(response.getStatus()).isEqualTo(AdoptionStatus.Processing);
        assertThat(response.getApplyReason()).isEqualTo("입양하고 싶습니다.");
        assertThat(response.getApplyTel()).isEqualTo("010-1234-5678");
        assertThat(response.getAnimalInfo().getDesertionNo()).isEqualTo("A-001");
        assertThat(response.getAnimalInfo().getCareNm()).isEqualTo("담당보호소");
    }

    @Test
    @DisplayName("다른 보호소의 입양 신청 상세는 조회할 수 없다")
    void getManagedShelterApplicationDetail_otherShelter() throws Exception {
        // given: 조회 요청자가 담당하는 보호소와 다른 보호소의 신청이 존재한다.
        User manager = createUser(1L, "manager@test.com");
        User otherManager = createUser(2L, "other-manager@test.com");
        User applicant = createUser(3L, "applicant@test.com");
        Shelter managedShelter = createShelter("S-001", "담당보호소", manager);
        Animal animal = createAnimal("A-001", "다른보호소", createShelter("S-002", "다른보호소", otherManager));
        AdoptionApplication application = createApplication(1L, applicant, animal);
        when(shelterRepository.findById("S-001"))
                .thenReturn(Optional.of(managedShelter));
        when(adoptionApplicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));

        // when & then
        assertThatThrownBy(() ->
                adoptionAdminService.getManagedShelterApplicationDetail(manager.getId(), "S-001", application.getId())
        )
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AdoptionErrorCode.FORBIDDEN_SHELTER_APPLICATION);
    }

    @Test
    @DisplayName("해당 careRegNo 보호소 관리자가 아니면 조회할 수 없다")
    void getManagedShelterApplicationDetail_notShelterManager() throws Exception {
        // given: 조회 요청자와 다른 관리자가 담당하는 보호소가 존재한다.
        User manager = createUser(1L, "manager@test.com");
        User otherManager = createUser(2L, "other-manager@test.com");
        Shelter otherShelter = createShelter("S-001", "다른보호소", otherManager);
        when(shelterRepository.findById("S-001"))
                .thenReturn(Optional.of(otherShelter));

        // when & then
        assertThatThrownBy(() ->
                adoptionAdminService.getManagedShelterApplicationDetail(manager.getId(), "S-001", 1L)
        )
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AdoptionErrorCode.UNAUTHORIZED_SHELTER);
    }

    @Test
    @DisplayName("담당 보호소의 입양 신청을 승인한다")
    void reviewApplication_approve() throws Exception {
        User manager = createUser(1L, "manager@test.com");
        User applicant = createUser(2L, "applicant@test.com");
        Animal animal = createAnimal("A-001", "담당보호소", createShelter("S-001", "담당보호소", manager));
        AdoptionApplication application = createApplication(1L, applicant, animal);
        when(shelterRepository.findById("S-001"))
                .thenReturn(Optional.of(animal.getShelter()));
        when(adoptionApplicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));

        AdoptionDetailRes response = adoptionAdminService.reviewApplication(
                manager.getId(),
                "S-001",
                application.getId(),
                new AdoptionReviewReq(AdoptionStatus.Approved, null)
        );

        assertThat(application.getStatus()).isEqualTo(AdoptionStatus.Approved);
        assertThat(application.getReviewedAt()).isNotNull();
        assertThat(application.getRejectionReason()).isNull();
        assertThat(response.getStatus()).isEqualTo(AdoptionStatus.Approved);
    }

    @Test
    @DisplayName("담당 보호소의 입양 신청을 거절한다")
    void reviewApplication_reject() throws Exception {
        User manager = createUser(1L, "manager@test.com");
        User applicant = createUser(2L, "applicant@test.com");
        Animal animal = createAnimal("A-001", "담당보호소", createShelter("S-001", "담당보호소", manager));
        AdoptionApplication application = createApplication(1L, applicant, animal);
        when(shelterRepository.findById("S-001"))
                .thenReturn(Optional.of(animal.getShelter()));
        when(adoptionApplicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));

        AdoptionDetailRes response = adoptionAdminService.reviewApplication(
                manager.getId(),
                "S-001",
                application.getId(),
                new AdoptionReviewReq(AdoptionStatus.Rejected, "조건이 맞지 않습니다.")
        );

        assertThat(application.getStatus()).isEqualTo(AdoptionStatus.Rejected);
        assertThat(application.getReviewedAt()).isNotNull();
        assertThat(application.getRejectionReason()).isEqualTo("조건이 맞지 않습니다.");
        assertThat(response.getStatus()).isEqualTo(AdoptionStatus.Rejected);
        assertThat(response.getRejectionReason()).isEqualTo("조건이 맞지 않습니다.");
    }

    @Test
    @DisplayName("담당 보호소의 입양 신청을 검토중으로 변경한다")
    void reviewApplication_markProcessing() throws Exception {
        User manager = createUser(1L, "manager@test.com");
        User applicant = createUser(2L, "applicant@test.com");
        Animal animal = createAnimal("A-001", "담당보호소", createShelter("S-001", "담당보호소", manager));
        AdoptionApplication application = createApplication(1L, applicant, animal);
        application.reject("이전 거절 사유");
        when(shelterRepository.findById("S-001"))
                .thenReturn(Optional.of(animal.getShelter()));
        when(adoptionApplicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));

        AdoptionDetailRes response = adoptionAdminService.reviewApplication(
                manager.getId(),
                "S-001",
                application.getId(),
                new AdoptionReviewReq(AdoptionStatus.Processing, null)
        );

        assertThat(application.getStatus()).isEqualTo(AdoptionStatus.Processing);
        assertThat(application.getReviewedAt()).isNull();
        assertThat(application.getRejectionReason()).isNull();
        assertThat(response.getStatus()).isEqualTo(AdoptionStatus.Processing);
        assertThat(response.getReviewedAt()).isNull();
        assertThat(response.getRejectionReason()).isNull();
    }

    @Test
    @DisplayName("거절 사유 없이 거절할 수 없다")
    void reviewApplication_rejectWithoutReason() throws Exception {
        User manager = createUser(1L, "manager@test.com");
        User applicant = createUser(2L, "applicant@test.com");
        Animal animal = createAnimal("A-001", "담당보호소", createShelter("S-001", "담당보호소", manager));
        AdoptionApplication application = createApplication(1L, applicant, animal);
        when(shelterRepository.findById("S-001"))
                .thenReturn(Optional.of(animal.getShelter()));
        when(adoptionApplicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));

        assertThatThrownBy(() -> adoptionAdminService.reviewApplication(
                manager.getId(),
                "S-001",
                application.getId(),
                new AdoptionReviewReq(AdoptionStatus.Rejected, " ")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AdoptionErrorCode.REJECTION_REASON_REQUIRED);
    }

    private User createUser(Long id, String email) throws Exception {
        User user = User.create(email, email, "홍길동");
        setId(user, id);
        return user;
    }

    private Shelter createShelter(String careRegNo, String careNm, User manager) {
        Shelter shelter = Shelter.create(new ShelterCommand(
                careRegNo,
                careNm,
                "010-0000-0000",
                "서울시",
                "보호소장",
                "서울시",
                LocalDateTime.now()
        ));
        shelter.assignUser(manager);
        return shelter;
    }

    private Animal createAnimal(String desertionNo, String careNm, Shelter shelter) {
        Animal animal = Animal.builder()
                .desertionNo(desertionNo)
                .processState("보호중")
                .stateGroup(0)
                .kindFullNm("믹스견")
                .specialMark("특이사항 없음")
                .careNm(careNm)
                .careOwnerNm("보호소장")
                .careTel("010-0000-0000")
                .careAddr("서울시")
                .totalCheerCount(0)
                .build();
        animal.assignShelter(shelter);
        return animal;
    }

    private AdoptionApplication createApplication(Long id, User applicant, Animal animal) throws Exception {
        AdoptionApplication application = AdoptionApplication.create(
                applicant,
                animal,
                "입양하고 싶습니다.",
                "010-1234-5678"
        );
        setId(application, id);
        return application;
    }

    private void setId(BaseEntity entity, Long id) throws Exception {
        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}
