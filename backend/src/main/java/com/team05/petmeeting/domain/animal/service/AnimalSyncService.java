package com.team05.petmeeting.domain.animal.service;

import com.team05.petmeeting.domain.animal.dto.AnimalSyncResponse;
import com.team05.petmeeting.domain.animal.dto.external.AnimalApiResponse;
import com.team05.petmeeting.domain.animal.dto.external.AnimalItem;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.format.DateTimeFormatter;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class AnimalSyncService {
    private static final LocalDate INITIAL_SYNC_START_DATE = LocalDate.of(2025, 1, 1);
    private static final long UPDATE_SYNC_DELAY_MS = 300L;
    private static final DateTimeFormatter API_UPDATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .toFormatter();
    private static final String SYNC_PAGE_MESSAGE = "유기동물 데이터 동기화 완료";
    private static final String INITIAL_SYNC_MESSAGE = "INITIAL_MONTHLY_SYNC_OK";
    private static final String UPDATE_SYNC_MESSAGE = "UPDATE_SYNC_OK";

    private final AnimalExternalService animalExternalService;
    private final AnimalRepository animalRepository;
    private final SyncStateRepository syncStateRepository;
    private final ShelterRepository shelterRepository;
    private final ShelterService shelterService;

    public AnimalSyncService(
            AnimalExternalService animalExternalService,
            AnimalRepository animalRepository,
            SyncStateRepository syncStateRepository,
            ShelterRepository shelterRepository,
            ShelterService shelterService
    ) {
        this.animalExternalService = animalExternalService;
        this.animalRepository = animalRepository;
        this.syncStateRepository = syncStateRepository;
        this.shelterRepository = shelterRepository;
        this.shelterService = shelterService;
    }

    private record SyncPageResult(
            String message,
            int savedCount,
            long elapsedMs
    ) {
    }

    // 특정 페이지를 한 번 조회해 동물 데이터를 저장한다.
    public AnimalSyncResponse fetchAndSaveAnimals(int pageNo, int numOfRows) {
        validatePageNo(pageNo);
        validateNumOfRows(numOfRows);

        try {
            SyncPageResult result = fetchAndInsertAnimals(pageNo, numOfRows, null, null, Integer.MAX_VALUE);
            log.info(
                    "Animal sync completed: pageNo={}, numOfRows={}, savedCount={}, elapsedMs={}",
                    pageNo,
                    numOfRows,
                    result.savedCount(),
                    result.elapsedMs()
            );
            return new AnimalSyncResponse(result.message(), result.savedCount(), result.elapsedMs());
        } catch (BusinessException e) {
            // 이미 도메인 예외로 분류된 검증 오류는 그대로 전파한다.
            throw e;
        } catch (RuntimeException e) {
            // 외부 API, 보호소 upsert, 동물 저장 중 발생한 예외는 동기화 실패 응답으로 변환한다.
            log.error("Animal sync failed: pageNo={}, numOfRows={}", pageNo, numOfRows, e);
            throw new BusinessException(AnimalErrorCode.ANIMAL_SYNC_FAILED, e);
        }
    }

    // 2008년 1월부터 현재까지 월 단위로 나눠 최초 적재를 수행한다.
    public AnimalSyncResponse runInitialMonthlySync(int numOfRows) {
        validateNumOfRows(numOfRows);

        try {
            Instant startedAt = Instant.now();
            int savedCount = fetchAndSaveMonthlyAnimalsFrom2008(numOfRows, Integer.MAX_VALUE);
            // 모든 월별 적재가 정상 종료된 뒤에만 최초 적재 성공 시각을 갱신한다.
            updateSyncState(AnimalSyncType.INITIAL);

            long elapsedMs = elapsedMs(startedAt);
            log.info(
                    "Initial animal sync completed: from={}, to={}, numOfRows={}, savedCount={}, elapsedMs={}",
                    INITIAL_SYNC_START_DATE,
                    LocalDate.now(),
                    numOfRows,
                    savedCount,
                    elapsedMs
            );
            return new AnimalSyncResponse(INITIAL_SYNC_MESSAGE, savedCount, elapsedMs);
        } catch (BusinessException e) {
            // 검증 단계에서 발생한 도메인 예외는 실패 원인을 유지한다.
            throw e;
        } catch (RuntimeException e) {
            // 일부 월 처리 중 실패하면 성공 상태로 저장하지 않고 최초 적재 실패로 응답한다.
            log.error("Initial animal sync failed: numOfRows={}", numOfRows, e);
            throw new BusinessException(AnimalErrorCode.INITIAL_ANIMAL_SYNC_FAILED, e);
        }
    }

    // 마지막 업데이트 시점 이후 수정된 데이터를 다시 반영한다.
    public AnimalSyncResponse runUpdateSync(int numOfRows) {
        validateNumOfRows(numOfRows);

        try {
            Instant startedAt = Instant.now();
            LocalDate bgupd = getUpdateStartDate();
            LocalDate enupd = LocalDate.now();
            int savedCount = fetchAndSaveAnimalsByUpdatedDate(bgupd, enupd, numOfRows);
            // 업데이트 반영이 끝난 경우에만 다음 업데이트 기준 시각을 갱신한다.
            updateSyncState(AnimalSyncType.UPDATE);

            long elapsedMs = elapsedMs(startedAt);
            log.info(
                    "Animal update sync completed: from={}, to={}, numOfRows={}, savedCount={}, elapsedMs={}",
                    bgupd,
                    enupd,
                    numOfRows,
                    savedCount,
                    elapsedMs
            );
            return new AnimalSyncResponse(UPDATE_SYNC_MESSAGE, savedCount, elapsedMs);
        } catch (BusinessException e) {
            // 검증 단계에서 발생한 도메인 예외는 실패 원인을 유지한다.
            throw e;
        } catch (RuntimeException e) {
            // 외부 API 조회나 저장 과정이 실패하면 업데이트 기준 시각을 앞당기지 않는다.
            log.error("Animal update sync failed: numOfRows={}", numOfRows, e);
            throw new BusinessException(AnimalErrorCode.UPDATE_ANIMAL_SYNC_FAILED, e);
        }
    }

    // 최초 적재 범위를 월별로 순회하면서 저장 건수를 누적한다.
    public int fetchAndSaveMonthlyAnimalsFrom2008(int numOfRows, int maxSaveCount) {
        LocalDate today = LocalDate.now();
        LocalDate currentMonthStart = INITIAL_SYNC_START_DATE.withDayOfMonth(1);
        int totalSavedCount = 0;

        while (!currentMonthStart.isAfter(today) && totalSavedCount < maxSaveCount) {
            LocalDate currentMonthEnd = currentMonthStart.withDayOfMonth(currentMonthStart.lengthOfMonth());
            if (currentMonthEnd.isAfter(today)) {
                currentMonthEnd = today;
            }

            totalSavedCount += fetchAndSaveAnimalsByDateRange(
                    currentMonthStart,
                    currentMonthEnd,
                    numOfRows,
                    maxSaveCount - totalSavedCount
            );
            currentMonthStart = currentMonthStart.plusMonths(1).withDayOfMonth(1);
        }

        return totalSavedCount;
    }

    // 수정일 기준 조회 결과를 페이지 단위로 끝까지 저장하거나 갱신한다.
    public int fetchAndSaveAnimalsByUpdatedDate(LocalDate bgupd, LocalDate enupd, int numOfRows) {
        int pageNo = 1;
        int totalSavedCount = 0;

        while (true) {
            AnimalApiResponse response = animalExternalService.fetchAnimalsByUpdatedDate(pageNo, numOfRows, bgupd, enupd);
            List<AnimalItem> items = extractItems(response);

            if (items.isEmpty()) {
                break;
            }

            int savedCount = saveOrUpdateAnimals(items);
            totalSavedCount += savedCount;
            pageNo++;
            waitForNextUpdatePage();
        }

        return totalSavedCount;
    }

    // 일반 조회 API를 한 번 호출해 현재 페이지 저장 결과를 만든다.
    private SyncPageResult fetchAndInsertAnimals(
            int pageNo,
            int numOfRows,
            LocalDate bgnde,
            LocalDate endde,
            int maxSaveCount
    ) {
        Instant startedAt = Instant.now();
        AnimalApiResponse response = animalExternalService.fetchAnimals(pageNo, numOfRows, bgnde, endde);
        List<AnimalItem> items = extractItems(response);

        if (items.isEmpty()) {
            return new SyncPageResult(SYNC_PAGE_MESSAGE, 0, elapsedMs(startedAt));
        }

        int savedCount = saveNewAnimals(items, maxSaveCount);
        return new SyncPageResult(SYNC_PAGE_MESSAGE, savedCount, elapsedMs(startedAt));
    }

    // 한 달 범위를 페이지별로 조회하면서 저장 가능한 동물을 누적한다.
    private int fetchAndSaveAnimalsByDateRange(LocalDate bgnde, LocalDate endde, int numOfRows, int maxSaveCount) {
        int pageNo = 1;
        int totalSavedCount = 0;

        while (totalSavedCount < maxSaveCount) {
            SyncPageResult result = fetchAndInsertAnimals(pageNo, numOfRows, bgnde, endde, maxSaveCount - totalSavedCount);
            if (result.savedCount() == 0) {
                break;
            }

            totalSavedCount += result.savedCount();
            pageNo++;
        }

        return totalSavedCount;
    }

    // 최초 적재에서는 보호소를 먼저 저장한 뒤, 현재 페이지의 신규 동물만 저장한다.
    private int saveNewAnimals(List<AnimalItem> items, int maxSaveCount) {
        syncShelters(items);

        List<Animal> animalsToSave = new ArrayList<>();
        Set<String> seenDesertionNos = new HashSet<>();

        for (AnimalItem item : items) {
            if (animalsToSave.size() >= maxSaveCount) {
                break;
            }

            if (item.getDesertionNo() == null || item.getDesertionNo().isBlank()) {
                continue;
            }

            if (!seenDesertionNos.add(item.getDesertionNo())) {
                continue;
            }

            // 보호소가 이미 저장돼 있으면 동물과 연관관계도 함께 연결한다.
            Animal animal = Animal.from(item);
            assignShelter(animal, item);
            animalsToSave.add(animal);
        }

        animalRepository.saveAll(animalsToSave);
        return animalsToSave.size();
    }

    // 업데이트 적재에서는 보호소를 먼저 저장한 뒤, 기존 동물은 수정하고 없으면 새로 저장한다.
    private int saveOrUpdateAnimals(List<AnimalItem> items) {
        syncShelters(items);

        int savedCount = 0;

        for (AnimalItem item : items) {
            if (item.getDesertionNo() == null || item.getDesertionNo().isBlank()) {
                continue;
            }

            animalRepository.findByDesertionNo(item.getDesertionNo())
                    .ifPresentOrElse(
                            animal -> {
                                animal.updateFrom(item);
                                // 기존 동물도 최신 보호소 정보에 맞춰 다시 연결한다.
                                assignShelter(animal, item);
                                animalRepository.save(animal);
                            },
                            () -> {
                                // 새 동물 저장 시에도 보호소 FK를 함께 채운다.
                                Animal animal = Animal.from(item);
                                assignShelter(animal, item);
                                animalRepository.save(animal);
                            }
                    );
            savedCount++;
        }

        return savedCount;
    }

    // 동물 저장 전에 보호소 정보를 먼저 upsert 한다.
    private void syncShelters(List<AnimalItem> items) {
        List<ShelterCommand> shelterCmds = items.stream()
                .filter(item -> item.getCareRegNo() != null && !item.getCareRegNo().isBlank())
                .filter(item -> item.getUpdTm() != null && !item.getUpdTm().isBlank())
                .map(item -> {
                    LocalDateTime updatedAt = parseApiUpdatedAt(item.getUpdTm());
                    if (updatedAt == null) {
                        log.warn("Skipping shelter sync for careRegNo={} due to invalid updTm={}", item.getCareRegNo(), item.getUpdTm());
                        return null;
                    }

                    return new ShelterCommand(
                            item.getCareRegNo(),
                            item.getCareNm(),
                            item.getCareTel(),
                            item.getCareAddr(),
                            item.getCareOwnerNm(),
                            item.getOrgNm(),
                            updatedAt
                    );
                })
                .filter(cmd -> cmd != null)
                .distinct()
                .toList();

        shelterService.createOrUpdateShelters(shelterCmds);
    }

    // careRegNo로 보호소를 찾아 Animal.shelter 연관관계를 연결한다.
    private void assignShelter(Animal animal, AnimalItem item) {
        if (item.getCareRegNo() == null || item.getCareRegNo().isBlank()) {
            return;
        }

        Shelter shelter = shelterRepository.findById(item.getCareRegNo()).orElse(null);
        if (shelter != null) {
            animal.assignShelter(shelter);
        }
    }

    // 외부 API 응답에서 실제 동물 목록만 꺼내고, 비정상 구조면 빈 리스트를 돌려준다.
    private List<AnimalItem> extractItems(AnimalApiResponse response) {
        if (response == null || response.getResponse() == null) {
            return List.of();
        }

        var body = response.getResponse().getBody();
        if (body == null || body.getItems() == null || body.getItems().getItem() == null) {
            return List.of();
        }

        return body.getItems().getItem();
    }

    // 마지막 UPDATE 동기화 시각을 기준으로 다음 업데이트 시작 날짜를 정한다.
    private LocalDate getUpdateStartDate() {
        LocalDateTime lastUpdatedAt = syncStateRepository.findBySyncType(AnimalSyncType.UPDATE)
                .map(SyncState::getLastUpdatedAt)
                .orElse(null);

        if (lastUpdatedAt == null) {
            return INITIAL_SYNC_START_DATE;
        }

        return lastUpdatedAt.toLocalDate();
    }

    // 외부 API 페이지 번호는 1부터 시작하므로 0 이하 요청을 차단한다.
    private void validatePageNo(int pageNo) {
        if (pageNo < 1) {
            throw new BusinessException(AnimalErrorCode.INVALID_PAGE_NUMBER);
        }
    }

    // 한 번에 조회할 건수가 없으면 적재를 시작할 수 없으므로 사전에 차단한다.
    private void validateNumOfRows(int numOfRows) {
        if (numOfRows < 1) {
            throw new BusinessException(AnimalErrorCode.INVALID_SYNC_REQUEST);
        }
    }

    // 동기화가 끝난 시각을 sync type 별로 저장한다.
    private void updateSyncState(AnimalSyncType syncType) {
        SyncState syncState = syncStateRepository.findBySyncType(syncType)
                .orElseGet(() -> SyncState.create(syncType));

        syncState.updateLastUpdatedAt(LocalDateTime.now());
        syncStateRepository.save(syncState);
    }

    // 업데이트 API를 연속 호출할 때 외부 서버 부담을 줄이기 위해 잠시 대기한다.
    private void waitForNextUpdatePage() {
        try {
            Thread.sleep(UPDATE_SYNC_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("업데이트 동기화 대기 중 인터럽트가 발생했습니다.", e);
        }
    }

    // 시작 시각부터 현재까지 걸린 시간을 밀리초로 계산한다.
    private long elapsedMs(Instant startedAt) {
        return Duration.between(startedAt, Instant.now()).toMillis();
    }

    private LocalDateTime parseApiUpdatedAt(String updTm) {
        try {
            return LocalDateTime.parse(updTm.trim(), API_UPDATE_TIME_FORMATTER);
        } catch (DateTimeException e) {
            return null;
        }
    }
}
