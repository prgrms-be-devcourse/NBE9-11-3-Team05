package com.team05.petmeeting.domain.animal.service;

import com.team05.petmeeting.domain.animal.dto.AnimalRes;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import com.team05.petmeeting.domain.shelter.dto.ShelterCommand;
import com.team05.petmeeting.domain.shelter.entity.Shelter;
import com.team05.petmeeting.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @InjectMocks
    private AnimalService animalService;

    @Mock
    private AnimalRepository animalRepository;

    @Nested
    @DisplayName("findByAnimalId 메서드는")
    class FindByAnimalId {

        @Test
        @DisplayName("존재하는 ID로 조회 시 Animal 엔티티를 반환한다.")
        void success() {
            // given
            Long animalId = 1L;
            Animal animal = Animal.builder().desertionNo("12345").build();
            given(animalRepository.findById(animalId)).willReturn(Optional.of(animal));

            // when
            Animal result = animalService.findByAnimalId(animalId);

            // then
            assertThat(result.getDesertionNo()).isEqualTo("12345");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 BusinessException을 던진다.")
        void fail_NotFound() {
            // given
            given(animalRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> animalService.findByAnimalId(999L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getAnimals 메서드는")
    class GetAnimals {

        @Test
        @DisplayName("필터 조건에 맞는 동물 목록을 페이징하여 반환한다.")
        void success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Shelter shelter = Shelter.create(
                    new ShelterCommand(
                            "CARE-123",
                            "서울 보호소",
                            "010-1234-5678",
                            "서울시 강남구",
                            "관리자",
                            "서울시",
                            LocalDateTime.now()
                    )
            );

            Animal animal = Animal.builder()
                    .desertionNo("123")
                    .totalCheerCount(10)
                    .shelter(shelter)
                    .build();
            Page<Animal> animalPage = new PageImpl<>(List.of(animal), pageable, 1);

            given(animalRepository.findAnimalsWithFilter(anyString(), anyString(), anyInt(), any(Pageable.class)))
                    .willReturn(animalPage);

            // when
            Page<AnimalRes> result = animalService.getAnimals("서울", "개", 0, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).desertionNo).isEqualTo("123");
        }

        @Test
        @DisplayName("페이지 번호가 음수일 경우 BusinessException을 던진다.")
        void fail_InvalidPageNumber() {
            // given
            //Pageable pageable = PageRequest.of(-1, 10);
            // PageRequest.of()를 쓰는 대신 Pageable을 Mock으로 만들어 객체 생성 시점의 검증을 우회
            Pageable pageable = mock(Pageable.class);

            // getPageNumber() 호출 시 음수(-1)를 반환하도록 설정
            given(pageable.getPageNumber()).willReturn(-1);

            // 실행 및 검증
            assertThatThrownBy(() -> animalService.getAnimals(null, null, null, pageable))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        // 에러 코드와 메시지를 직접 꺼내서 확인
                        assertThat(be.getErrorCode().getCode()).isEqualTo("A-002");
                        assertThat(be.getErrorCode().getMessage()).isEqualTo("잘못된 페이지 번호입니다");
                    });
        }
    }
}
