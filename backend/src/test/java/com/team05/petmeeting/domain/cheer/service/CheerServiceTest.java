package com.team05.petmeeting.domain.cheer.service;

import com.team05.petmeeting.domain.animal.dto.external.AnimalItem;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import com.team05.petmeeting.domain.cheer.dto.CheerRes;
import com.team05.petmeeting.domain.cheer.entity.Cheer;
import com.team05.petmeeting.domain.cheer.errorCode.CheerErrorCode;
import com.team05.petmeeting.domain.cheer.repository.CheerRepository;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheerService 단위 테스트")
class CheerServiceTest {

    @Mock
    private CheerRepository cheerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AnimalRepository animalRepository;

    @InjectMocks
    private CheerService cheerService;

    private User testUser;
    private Animal testAnimal;

    @BeforeEach
    void setUp() {
        // ID 1L인 유저 생성
        testUser = createUser(1L, 0, LocalDate.now());
        // ID 100L인 동물 생성
        testAnimal = createAnimal(100L, 0);
    }

    private User createUser(Long id, int heartCount, LocalDate resetDate) {
        User user = User.create("test@test.com", "테스터", "홍길동");
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "dailyHeartCount", heartCount);
        ReflectionTestUtils.setField(user, "lastHeartResetDate", resetDate);
        return user;
    }

    private Animal createAnimal(Long id, int totalCheerCount) {
        Animal animal = Animal.from(new AnimalItem()); // AnimalItem 구조에 따라 필드 채움 가정
        ReflectionTestUtils.setField(animal, "id", id);
        ReflectionTestUtils.setField(animal, "totalCheerCount", totalCheerCount);
        return animal;
    }

    @Test
    @DisplayName("cheerAnimal: 정상 동작")
    void cheerAnimal_success() {
        // given: userId=1L, animalId=100L
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(animalRepository.findById(100L)).willReturn(Optional.of(testAnimal));

        // save 시 인자를 그대로 반환하도록 설정 (컴파일 에러 해결 버전)
        given(cheerRepository.save(any(Cheer.class)))
                .willAnswer(invocation -> invocation.getArgument(0, Cheer.class));

        // when: 서비스의 파라미터 순서(userId, animalId)에 맞춰 호출!
        CheerRes result = cheerService.cheerAnimal(1L, 100L);

        // then
        assertThat(result.getAnimalId()).isEqualTo(100L);
        assertThat(result.getRemaingCheersToday()).isEqualTo(4); // 5 - 1
        assertThat(testUser.getDailyHeartCount()).isEqualTo(1);

        verify(cheerRepository).save(any(Cheer.class));
        verify(animalRepository).incrementCheerCount(100L);
    }

    @Test
    @DisplayName("cheerAnimal: 일일 응원 제한 초과")
    void cheerAnimal_limitExceeded() {
        // given: 이미 5번 응원한 상태
        testUser = createUser(1L, 5, LocalDate.now());
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(animalRepository.findById(100L)).willReturn(Optional.of(testAnimal));

        // when & then
        assertThatThrownBy(() -> cheerService.cheerAnimal(1L, 100L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CheerErrorCode.DAILY_CHEER_LIMIT_EXCEEDED);

        verify(cheerRepository, never()).save(any());
    }

    @Test
    @DisplayName("cheerAnimal: Cheer 객체를 사용자와 동물로 생성해 저장한다")
    void cheerAnimal_savesCheerWithUserAndAnimal() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(animalRepository.findById(100L)).willReturn(Optional.of(testAnimal));
        given(cheerRepository.save(any(Cheer.class)))
                .willAnswer(invocation -> invocation.getArgument(0, Cheer.class));

        // when
        cheerService.cheerAnimal(1L, 100L);

        // then: 실제 저장된 Cheer 객체 내부의 관계 검증
        verify(cheerRepository).save(argThat(cheer ->
                cheer.getUser().equals(testUser) &&
                        cheer.getAnimal().equals(testAnimal)
        ));
    }

    @Test
    @DisplayName("cheerAnimal: 전날 응원 기록이 있어도 자정이 지나면 초기화 후 응원 가능하다")
    void cheerAnimal_resetAfterMidnight() {
        // 1. Given: 마지막 응원 날짜가 '어제'이고, 이미 5번을 다 쓴 유저
        LocalDate yesterday = LocalDate.now().minusDays(1);
        testUser = createUser(1L, 5, yesterday);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(animalRepository.findById(100L)).willReturn(Optional.of(testAnimal));
        given(cheerRepository.save(any(Cheer.class)))
                .willAnswer(invocation -> invocation.getArgument(0, Cheer.class));

        // 2. When: 응원 시도
        CheerRes result = cheerService.cheerAnimal(1L, 100L);

        // 3. Then
        // 응원이 성공하여 남은 횟수가 4가 되어야 함 (5회 중 1회 사용)
        assertThat(result.getRemaingCheersToday()).isEqualTo(4);

        // 유저의 상태가 오늘 날짜로 갱신되었는지 확인
        assertThat(testUser.getDailyHeartCount()).isEqualTo(1);
        assertThat(testUser.getLastHeartResetDate()).isEqualTo(LocalDate.now());

        verify(cheerRepository, times(1)).save(any(Cheer.class));
    }
}