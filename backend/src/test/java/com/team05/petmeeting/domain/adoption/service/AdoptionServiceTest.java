package com.team05.petmeeting.domain.adoption.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team05.petmeeting.domain.adoption.dto.request.AdoptionApplyRequest;
import com.team05.petmeeting.domain.adoption.dto.response.AdoptionApplyResponse;
import com.team05.petmeeting.domain.adoption.dto.response.AdoptionDetailResponse;
import com.team05.petmeeting.domain.adoption.entity.AdoptionApplication;
import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus;
import com.team05.petmeeting.domain.adoption.errorCode.AdoptionErrorCode;
import com.team05.petmeeting.domain.adoption.repository.AdoptionApplicationRepository;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.entity.BaseEntity;
import com.team05.petmeeting.global.exception.BusinessException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
// 일반 사용자가 입양 신청서를 조회, 제출, 취소하는 서비스 흐름을 검증한다.
class AdoptionServiceTest {

    @InjectMocks
    private AdoptionService adoptionService;

    @Mock
    private AdoptionApplicationRepository adoptionApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Test
    @DisplayName("로그인 사용자의 입양 신청 목록을 조회한다")
    void getMyAdoptions() throws Exception {
        User user = createUser(1L);
        Animal animal = createAnimal(10L, "A-001");
        AdoptionApplication application = createApplication(100L, user, animal);
        when(adoptionApplicationRepository.findByUser_Id(user.getId()))
                .thenReturn(List.of(application));

        List<AdoptionApplyResponse> responses = adoptionService.getMyAdoptions(user.getId());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getApplicationId()).isEqualTo(100L);
        assertThat(responses.get(0).getStatus()).isEqualTo(AdoptionStatus.Processing);
        assertThat(responses.get(0).getAnimalInfo().getDesertionNo()).isEqualTo("A-001");
        assertThat(responses.get(0).getAnimalInfo().getCareNm()).isEqualTo("테스트 보호소");
    }

    @Test
    @DisplayName("로그인 사용자가 본인의 입양 신청 상세를 조회한다")
    void getApplicationDetail() throws Exception {
        User user = createUser(1L);
        Animal animal = createAnimal(10L, "A-001");
        AdoptionApplication application = createApplication(100L, user, animal);
        when(adoptionApplicationRepository.findByIdAndUser_Id(application.getId(), user.getId()))
                .thenReturn(Optional.of(application));

        AdoptionDetailResponse response = adoptionService.getApplicationDetail(user.getId(), application.getId());

        assertThat(response.getApplicationId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(AdoptionStatus.Processing);
        assertThat(response.getApplyReason()).isEqualTo("입양하고 싶습니다.");
        assertThat(response.getApplyTel()).isEqualTo("010-1234-5678");
        assertThat(response.getAnimalInfo().getDesertionNo()).isEqualTo("A-001");
        assertThat(response.getAnimalInfo().getCareTel()).isEqualTo("010-0000-0000");
    }

    @Test
    @DisplayName("다른 사용자의 입양 신청 상세는 조회할 수 없다")
    void getApplicationDetail_notOwner() {
        // 본인 userId와 applicationId가 함께 매칭되지 않으면 신청서가 없는 것으로 처리한다.
        when(adoptionApplicationRepository.findByIdAndUser_Id(100L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adoptionService.getApplicationDetail(1L, 100L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AdoptionErrorCode.APPLICATION_NOT_FOUND);
    }

    @Test
    @DisplayName("로그인 사용자가 입양 신청서를 제출한다")
    void applyApplication() throws Exception {
        // 중복 신청 여부를 확인한 뒤 사용자와 동물이 모두 존재할 때 신청서를 저장한다.
        User user = createUser(1L);
        Animal animal = createAnimal(10L, "A-001");
        AdoptionApplyRequest request = createApplyRequest("가족으로 맞이하고 싶습니다.", "010-9999-8888");
        when(adoptionApplicationRepository.existsByUser_IdAndAnimal_Id(user.getId(), animal.getId()))
                .thenReturn(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(animalRepository.findById(animal.getId())).thenReturn(Optional.of(animal));
        when(adoptionApplicationRepository.save(org.mockito.ArgumentMatchers.any(AdoptionApplication.class)))
                .thenAnswer(invocation -> {
                    AdoptionApplication saved = invocation.getArgument(0);
                    setId(saved, 100L);
                    return saved;
                });

        AdoptionApplyResponse response = adoptionService.applyApplication(user.getId(), animal.getId(), request);

        assertThat(response.getApplicationId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(AdoptionStatus.Processing);
        assertThat(response.getAnimalInfo().getDesertionNo()).isEqualTo("A-001");
        verify(adoptionApplicationRepository).save(org.mockito.ArgumentMatchers.any(AdoptionApplication.class));
    }

    @Test
    @DisplayName("이미 신청한 동물은 다시 신청할 수 없다")
    void applyApplication_alreadyApplied() {
        // 같은 사용자와 같은 동물 조합의 신청이 이미 있으면 저장까지 진행하지 않는다.
        when(adoptionApplicationRepository.existsByUser_IdAndAnimal_Id(1L, 10L))
                .thenReturn(true);

        assertThatThrownBy(() -> adoptionService.applyApplication(1L, 10L, createApplyRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AdoptionErrorCode.ALREADY_APPLIED);
        verify(adoptionApplicationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 입양 신청할 수 없다")
    void applyApplication_userNotFound() {
        when(adoptionApplicationRepository.existsByUser_IdAndAnimal_Id(1L, 10L))
                .thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adoptionService.applyApplication(1L, 10L, createApplyRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 동물에는 입양 신청할 수 없다")
    void applyApplication_animalNotFound() throws Exception {
        User user = createUser(1L);
        when(adoptionApplicationRepository.existsByUser_IdAndAnimal_Id(1L, 10L))
                .thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(animalRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adoptionService.applyApplication(1L, 10L, createApplyRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AnimalErrorCode.ANIMAL_NOT_FOUND);
    }

    @Test
    @DisplayName("로그인 사용자가 본인의 입양 신청을 취소한다")
    void cancelApplication() throws Exception {
        // 삭제도 상세 조회와 동일하게 본인 userId와 applicationId가 함께 매칭되는 신청서만 허용한다.
        User user = createUser(1L);
        AdoptionApplication application = createApplication(100L, user, createAnimal(10L, "A-001"));
        when(adoptionApplicationRepository.findByIdAndUser_Id(application.getId(), user.getId()))
                .thenReturn(Optional.of(application));

        adoptionService.cancelApplication(user.getId(), application.getId());

        verify(adoptionApplicationRepository).delete(application);
    }

    @Test
    @DisplayName("다른 사용자의 입양 신청은 취소할 수 없다")
    void cancelApplication_notOwner() {
        when(adoptionApplicationRepository.findByIdAndUser_Id(100L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adoptionService.cancelApplication(1L, 100L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(AdoptionErrorCode.APPLICATION_NOT_FOUND);
    }

    private User createUser(Long id) throws Exception {
        User user = User.create("user@test.com", "사용자", "홍길동");
        setId(user, id);
        return user;
    }

    private Animal createAnimal(Long id, String desertionNo) throws Exception {
        Animal animal = Animal.builder()
                .desertionNo(desertionNo)
                .processState("보호중")
                .stateGroup(0)
                .kindFullNm("믹스견")
                .specialMark("특이사항 없음")
                .careNm("테스트 보호소")
                .careOwnerNm("보호소장")
                .careTel("010-0000-0000")
                .careAddr("서울시")
                .totalCheerCount(0)
                .build();
        setId(animal, id);
        return animal;
    }

    private AdoptionApplication createApplication(Long id, User user, Animal animal) throws Exception {
        AdoptionApplication application = AdoptionApplication.create(
                user,
                animal,
                "입양하고 싶습니다.",
                "010-1234-5678"
        );
        setId(application, id);
        return application;
    }

    private AdoptionApplyRequest createApplyRequest() {
        return createApplyRequest("입양하고 싶습니다.", "010-1234-5678");
    }

    private AdoptionApplyRequest createApplyRequest(String applyReason, String applyTel) {
        // 요청 DTO는 기본 생성자와 getter만 있어 테스트에서는 필드를 직접 세팅한다.
        AdoptionApplyRequest request = new AdoptionApplyRequest();
        ReflectionTestUtils.setField(request, "applyReason", applyReason);
        ReflectionTestUtils.setField(request, "applyTel", applyTel);
        return request;
    }

    private void setId(BaseEntity entity, Long id) throws Exception {
        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}
