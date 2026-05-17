package com.team05.petmeeting.domain.animal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team05.petmeeting.domain.animal.dto.AnimalSyncResponse;
import com.team05.petmeeting.domain.animal.dto.external.AnimalApiResponse;
import com.team05.petmeeting.domain.animal.dto.external.AnimalBody;
import com.team05.petmeeting.domain.animal.dto.external.AnimalItem;
import com.team05.petmeeting.domain.animal.dto.external.AnimalItems;
import com.team05.petmeeting.domain.animal.dto.external.AnimalResponse;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.entity.AnimalSyncType;
import com.team05.petmeeting.domain.animal.entity.SyncState;
import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import com.team05.petmeeting.domain.animal.repository.SyncStateRepository;
import com.team05.petmeeting.domain.shelter.dto.ShelterCommand;
import com.team05.petmeeting.domain.shelter.entity.Shelter;
import com.team05.petmeeting.domain.shelter.repository.ShelterRepository;
import com.team05.petmeeting.domain.shelter.service.ShelterService;
import com.team05.petmeeting.global.exception.BusinessException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AnimalSyncServiceTest {

    @InjectMocks
    private AnimalSyncService animalSyncService;

    @Mock
    private AnimalExternalService animalExternalService;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private SyncStateRepository syncStateRepository;

    @Mock
    private ShelterRepository shelterRepository;

    @Mock
    private ShelterService shelterService;

    @Test
    @DisplayName("업데이트 동기화 - 마지막 UPDATE 시점 이후 데이터를 기존 동물 갱신과 신규 동물 저장으로 반영한다")
    void runUpdateSync_updatesExistingAndCreatesNewAnimal() {
        // given
        LocalDateTime lastUpdatedAt = LocalDateTime.of(2026, 4, 20, 13, 30);
        SyncState updateState = SyncState.create(AnimalSyncType.UPDATE);
        updateState.updateLastUpdatedAt(lastUpdatedAt);

        AnimalItem updatedItem = animalItem(
                "D-001",
                "종료(입양)",
                "NOTICE-UPDATED",
                "20260430",
                "새 발견장소",
                "믹스견",
                "새 보호소",
                "010-2222-3333",
                "2026-04-21 10:00:00.0",
                "CARE-001"
        );
        AnimalItem newItem = animalItem(
                "D-002",
                "보호중",
                "NOTICE-NEW",
                "20260430",
                "신규 발견장소",
                "코리안숏헤어",
                "새 보호소",
                "010-2222-3333",
                "2026-04-21 11:00:00.0",
                "CARE-001"
        );

        Animal existingAnimal = Animal.from(animalItem(
                "D-001",
                "보호중",
                "NOTICE-OLD",
                "20260410",
                "기존 발견장소",
                "믹스견",
                "기존 보호소",
                "010-1111-2222",
                "2026-04-19 09:00:00.0",
                "CARE-OLD"
        ));
        Shelter shelter = Shelter.create(new ShelterCommand(
                "CARE-001",
                "새 보호소",
                "010-2222-3333",
                "보호소 주소",
                "담당자",
                "기관",
                LocalDateTime.of(2026, 4, 21, 10, 0)
        ));

        when(syncStateRepository.findBySyncType(AnimalSyncType.UPDATE)).thenReturn(Optional.of(updateState));
        when(animalExternalService.fetchAnimalsByUpdatedDate(
                eq(1),
                eq(10),
                eq(lastUpdatedAt.toLocalDate()),
                eq(LocalDate.now())
        )).thenReturn(apiResponse(List.of(updatedItem, newItem)));
        when(animalExternalService.fetchAnimalsByUpdatedDate(
                eq(2),
                eq(10),
                eq(lastUpdatedAt.toLocalDate()),
                eq(LocalDate.now())
        )).thenReturn(apiResponse(List.of()));
        when(animalRepository.findByDesertionNo("D-001")).thenReturn(Optional.of(existingAnimal));
        when(animalRepository.findByDesertionNo("D-002")).thenReturn(Optional.empty());
        when(shelterRepository.findById("CARE-001")).thenReturn(Optional.of(shelter));

        // when
        AnimalSyncResponse response = animalSyncService.runUpdateSync(10);

        // then
        assertThat(response.message()).isEqualTo("UPDATE_SYNC_OK");
        assertThat(response.savedCount()).isEqualTo(2);

        assertThat(existingAnimal.getProcessState()).isEqualTo("종료(입양)");
        assertThat(existingAnimal.getNoticeNo()).isEqualTo("NOTICE-UPDATED");
        assertThat(existingAnimal.getHappenPlace()).isEqualTo("새 발견장소");
        assertThat(existingAnimal.getCareNm()).isEqualTo("새 보호소");
        assertThat(existingAnimal.getShelter()).isEqualTo(shelter);

        ArgumentCaptor<Animal> animalCaptor = ArgumentCaptor.forClass(Animal.class);
        verify(animalRepository, times(2)).save(animalCaptor.capture());
        List<Animal> savedAnimals = animalCaptor.getAllValues();
        assertThat(savedAnimals)
                .extracting(Animal::getDesertionNo)
                .containsExactlyInAnyOrder("D-001", "D-002");
        assertThat(savedAnimals.stream()
                .filter(animal -> animal.getDesertionNo().equals("D-002"))
                .findFirst()
                .orElseThrow()
                .getShelter()).isEqualTo(shelter);

        verify(shelterService).createOrUpdateShelters(anyList());
        verify(syncStateRepository).save(updateState);
        assertThat(updateState.getLastUpdatedAt()).isAfter(lastUpdatedAt);
    }

    @Test
    @DisplayName("업데이트 동기화 - updTm에 소수점이 없어도 정상 반영한다")
    void runUpdateSync_acceptsUpdTmWithoutFraction() {
        LocalDateTime lastUpdatedAt = LocalDateTime.of(2026, 4, 20, 13, 30);
        SyncState updateState = SyncState.create(AnimalSyncType.UPDATE);
        updateState.updateLastUpdatedAt(lastUpdatedAt);

        AnimalItem item = animalItem(
                "D-003",
                "보호중",
                "NOTICE-003",
                "20260430",
                "발견장소",
                "믹스견",
                "보호소",
                "010-1234-5678",
                "2026-04-21 10:00:00",
                "CARE-003"
        );
        Shelter shelter = Shelter.create(new ShelterCommand(
                "CARE-003",
                "보호소",
                "010-1234-5678",
                "보호소 주소",
                "담당자",
                "기관",
                LocalDateTime.of(2026, 4, 21, 10, 0)
        ));

        when(syncStateRepository.findBySyncType(AnimalSyncType.UPDATE)).thenReturn(Optional.of(updateState));
        when(animalExternalService.fetchAnimalsByUpdatedDate(
                eq(1),
                eq(10),
                eq(lastUpdatedAt.toLocalDate()),
                eq(LocalDate.now())
        )).thenReturn(apiResponse(List.of(item)));
        when(animalExternalService.fetchAnimalsByUpdatedDate(
                eq(2),
                eq(10),
                eq(lastUpdatedAt.toLocalDate()),
                eq(LocalDate.now())
        )).thenReturn(apiResponse(List.of()));
        when(animalRepository.findByDesertionNo("D-003")).thenReturn(Optional.empty());
        when(shelterRepository.findById("CARE-003")).thenReturn(Optional.of(shelter));

        AnimalSyncResponse response = animalSyncService.runUpdateSync(10);

        assertThat(response.message()).isEqualTo("UPDATE_SYNC_OK");
        assertThat(response.savedCount()).isEqualTo(1);
        verify(animalRepository).save(any(Animal.class));
        verify(shelterService).createOrUpdateShelters(anyList());
    }

    @Test
    @DisplayName("업데이트 동기화 - 마지막 UPDATE 시점이 없으면 2008-01-01부터 조회하고 상태를 새로 저장한다")
    void runUpdateSync_withoutPreviousStateStartsFromInitialDate() {
        // given
        when(syncStateRepository.findBySyncType(AnimalSyncType.UPDATE)).thenReturn(Optional.empty());
        when(animalExternalService.fetchAnimalsByUpdatedDate(
                eq(1),
                eq(20),
                eq(LocalDate.of(2008, 1, 1)),
                eq(LocalDate.now())
        )).thenReturn(apiResponse(List.of()));

        // when
        AnimalSyncResponse response = animalSyncService.runUpdateSync(20);

        // then
        assertThat(response.message()).isEqualTo("UPDATE_SYNC_OK");
        assertThat(response.savedCount()).isZero();

        ArgumentCaptor<SyncState> syncStateCaptor = ArgumentCaptor.forClass(SyncState.class);
        verify(syncStateRepository).save(syncStateCaptor.capture());
        assertThat(syncStateCaptor.getValue().getSyncType()).isEqualTo(AnimalSyncType.UPDATE);
        assertThat(syncStateCaptor.getValue().getLastUpdatedAt()).isNotNull();
        verify(animalRepository, never()).save(any(Animal.class));
        verify(shelterService, never()).createOrUpdateShelters(anyList());
    }

    @Test
    @DisplayName("업데이트 동기화 - 외부 API 오류가 발생하면 업데이트 실패 예외로 변환하고 상태를 갱신하지 않는다")
    void runUpdateSync_whenExternalApiFailsThrowsBusinessException() {
        // given
        LocalDateTime lastUpdatedAt = LocalDateTime.of(2026, 4, 20, 13, 30);
        SyncState updateState = SyncState.create(AnimalSyncType.UPDATE);
        updateState.updateLastUpdatedAt(lastUpdatedAt);

        when(syncStateRepository.findBySyncType(AnimalSyncType.UPDATE)).thenReturn(Optional.of(updateState));
        when(animalExternalService.fetchAnimalsByUpdatedDate(
                eq(1),
                eq(10),
                eq(lastUpdatedAt.toLocalDate()),
                eq(LocalDate.now())
        )).thenThrow(new IllegalStateException("외부 API 오류"));

        // when & then
        assertThatThrownBy(() -> animalSyncService.runUpdateSync(10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AnimalErrorCode.UPDATE_ANIMAL_SYNC_FAILED);

        verify(syncStateRepository, never()).save(any(SyncState.class));
        verify(animalRepository, never()).save(any(Animal.class));
    }

    @Test
    @DisplayName("페이지 동기화 - 잘못된 페이지 번호면 적재를 시작하지 않고 검증 예외를 던진다")
    void fetchAndSaveAnimals_withInvalidPageNoThrowsBusinessException() {
        assertThatThrownBy(() -> animalSyncService.fetchAndSaveAnimals(0, 10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AnimalErrorCode.INVALID_PAGE_NUMBER);

        verify(animalExternalService, never()).fetchAnimals(anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("초기 적재 - 잘못된 조회 건수면 적재를 시작하지 않고 검증 예외를 던진다")
    void runInitialMonthlySync_withInvalidNumOfRowsThrowsBusinessException() {
        assertThatThrownBy(() -> animalSyncService.runInitialMonthlySync(0))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AnimalErrorCode.INVALID_SYNC_REQUEST);

        verify(animalExternalService, never()).fetchAnimals(anyInt(), anyInt(), any(), any());
        verify(syncStateRepository, never()).save(any(SyncState.class));
    }

    private AnimalApiResponse apiResponse(List<AnimalItem> itemList) {
        AnimalItems items = new AnimalItems();
        ReflectionTestUtils.setField(items, "item", itemList);

        AnimalBody body = new AnimalBody();
        ReflectionTestUtils.setField(body, "items", items);

        AnimalResponse response = new AnimalResponse();
        ReflectionTestUtils.setField(response, "body", body);

        AnimalApiResponse apiResponse = new AnimalApiResponse();
        ReflectionTestUtils.setField(apiResponse, "response", response);
        return apiResponse;
    }

    private AnimalItem animalItem(
            String desertionNo,
            String processState,
            String noticeNo,
            String noticeEdt,
            String happenPlace,
            String kindFullNm,
            String careNm,
            String careTel,
            String updTm,
            String careRegNo
    ) {
        AnimalItem item = new AnimalItem();
        ReflectionTestUtils.setField(item, "desertionNo", desertionNo);
        ReflectionTestUtils.setField(item, "processState", processState);
        ReflectionTestUtils.setField(item, "noticeNo", noticeNo);
        ReflectionTestUtils.setField(item, "noticeEdt", noticeEdt);
        ReflectionTestUtils.setField(item, "happenPlace", happenPlace);
        ReflectionTestUtils.setField(item, "upKindNm", "개");
        ReflectionTestUtils.setField(item, "kindFullNm", kindFullNm);
        ReflectionTestUtils.setField(item, "colorCd", "흰색");
        ReflectionTestUtils.setField(item, "age", "2024(년생)");
        ReflectionTestUtils.setField(item, "weight", "5(Kg)");
        ReflectionTestUtils.setField(item, "sexCd", "M");
        ReflectionTestUtils.setField(item, "popfile1", "https://example.com/image1.jpg");
        ReflectionTestUtils.setField(item, "popfile2", "https://example.com/image2.jpg");
        ReflectionTestUtils.setField(item, "specialMark", "온순함");
        ReflectionTestUtils.setField(item, "careOwnerNm", "담당자");
        ReflectionTestUtils.setField(item, "careNm", careNm);
        ReflectionTestUtils.setField(item, "careAddr", "보호소 주소");
        ReflectionTestUtils.setField(item, "careTel", careTel);
        ReflectionTestUtils.setField(item, "updTm", updTm);
        ReflectionTestUtils.setField(item, "careRegNo", careRegNo);
        ReflectionTestUtils.setField(item, "orgNm", "기관");
        return item;
    }
}
