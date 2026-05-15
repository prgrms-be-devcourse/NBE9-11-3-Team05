package com.team05.petmeeting.domain.cheer.service

import com.team05.petmeeting.domain.animal.dto.external.AnimalItem
import com.team05.petmeeting.domain.animal.entity.Animal
import com.team05.petmeeting.domain.animal.entity.Animal.Companion.from
import com.team05.petmeeting.domain.animal.repository.AnimalRepository
import com.team05.petmeeting.domain.cheer.entity.Cheer
import com.team05.petmeeting.domain.cheer.errorCode.CheerErrorCode
import com.team05.petmeeting.domain.cheer.repository.CheerRepository
import com.team05.petmeeting.domain.user.entity.User
import com.team05.petmeeting.domain.user.repository.UserRepository
import com.team05.petmeeting.global.exception.BusinessException
import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.stubbing.Answer
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("CheerService 단위 테스트")
internal class CheerServiceTest(
    @Mock
    private val cheerRepository: CheerRepository,
    @Mock
    private val userRepository: UserRepository,
    @Mock
    private val animalRepository: AnimalRepository,
) {

    @InjectMocks
    lateinit var cheerService: CheerService

    lateinit var testUser: User
    lateinit var testAnimal: Animal

    @BeforeEach
    fun setUp() {
        // ID 1L인 유저 생성
        testUser = createUser(1L, 0, LocalDate.now())
        // ID 100L인 동물 생성
        testAnimal = createAnimal(100L, 0)
    }

    private fun createUser(id: Long, heartCount: Int, resetDate: LocalDate): User {
        val user = User.create("test@test.com", "테스터", "홍길동")
        ReflectionTestUtils.setField(user, "id", id)
        ReflectionTestUtils.setField(user, "dailyHeartCount", heartCount)
        ReflectionTestUtils.setField(user, "lastHeartResetDate", resetDate)
        return user
    }

    private fun createAnimal(id: Long, totalCheerCount: Int): Animal {
        val animal = from(AnimalItem()) // AnimalItem 구조에 따라 필드 채움 가정
        ReflectionTestUtils.setField(animal, "id", id)
        ReflectionTestUtils.setField(animal, "totalCheerCount", totalCheerCount)
        return animal
    }

    @Test
    @DisplayName("cheerAnimal: 정상 동작")
    fun cheerAnimal_success() {
        // given: userId=1L, animalId=100L
        BDDMockito.given(userRepository.findById(1L)).willReturn(Optional.of(testUser))
        BDDMockito.given(animalRepository.findById(100L))
            .willReturn(Optional.of(testAnimal))

        // save 시 인자를 그대로 반환하도록 설정 (컴파일 에러 해결 버전)
        BDDMockito.given(cheerRepository.save(ArgumentMatchers.any(Cheer::class.java)))
            .willAnswer(Answer { invocation: InvocationOnMock? ->
                invocation!!.getArgument(
                    0,
                    Cheer::class.java
                )
            })

        // when: 서비스의 파라미터 순서(userId, animalId)에 맞춰 호출!
        val result = cheerService.cheerAnimal(1L, 100L)

        // then
        Assertions.assertThat(result.animalId).isEqualTo(100L)
        Assertions.assertThat(result.remaingCheersToday).isEqualTo(4) // 5 - 1
        Assertions.assertThat(testUser.dailyHeartCount).isEqualTo(1)

        Mockito.verify(cheerRepository).save(ArgumentMatchers.any(Cheer::class.java))
        Mockito.verify(animalRepository).incrementCheerCount(100L)
    }

    @Test
    @DisplayName("cheerAnimal: 일일 응원 제한 초과")
    fun cheerAnimal_limitExceeded() {
        // given: 이미 5번 응원한 상태
        testUser = createUser(1L, 5, LocalDate.now())
        BDDMockito.given(userRepository.findById(1L)).willReturn(Optional.of(testUser))
        BDDMockito.given(animalRepository.findById(100L))
            .willReturn(Optional.of(testAnimal))

        // when & then
        Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable { cheerService.cheerAnimal(1L, 100L) })
            .isInstanceOf(BusinessException::class.java)
            .extracting("errorCode")
            .isEqualTo(CheerErrorCode.DAILY_CHEER_LIMIT_EXCEEDED)

        Mockito.verify(cheerRepository, Mockito.never()).save(ArgumentMatchers.any())
    }

    @Test
    @DisplayName("cheerAnimal: Cheer 객체를 사용자와 동물로 생성해 저장한다")
    fun cheerAnimal_savesCheerWithUserAndAnimal() {
        // given
        BDDMockito.given(userRepository.findById(1L)).willReturn(Optional.of(testUser))
        BDDMockito.given(animalRepository.findById(100L))
            .willReturn(Optional.of(testAnimal))
        BDDMockito.given(cheerRepository.save(ArgumentMatchers.any(Cheer::class.java)))
            .willAnswer(Answer { invocation: InvocationOnMock? ->
                invocation!!.getArgument(
                    0,
                    Cheer::class.java
                )
            })

        // when
        cheerService.cheerAnimal(1L, 100L)

        // then: 실제 저장된 Cheer 객체 내부의 관계 검증
        Mockito.verify(cheerRepository)
            .save(ArgumentMatchers.argThat(ArgumentMatcher { cheer: Cheer ->
                cheer.user == testUser &&
                        cheer.animal == testAnimal
            }
            ))
    }

    @Test
    @DisplayName("cheerAnimal: 전날 응원 기록이 있어도 자정이 지나면 초기화 후 응원 가능하다")
    fun cheerAnimal_resetAfterMidnight() {
        // 1. Given: 마지막 응원 날짜가 '어제'이고, 이미 5번을 다 쓴 유저
        val yesterday = LocalDate.now().minusDays(1)
        testUser = createUser(1L, 5, yesterday)

        BDDMockito.given(userRepository.findById(1L)).willReturn(Optional.of<User>(testUser))
        BDDMockito.given(animalRepository.findById(100L))
            .willReturn(Optional.of(testAnimal))
        BDDMockito.given(cheerRepository.save(ArgumentMatchers.any(Cheer::class.java)))
            .willAnswer(Answer { invocation: InvocationOnMock? ->
                invocation!!.getArgument(
                    0,
                    Cheer::class.java
                )
            })

        // 2. When: 응원 시도
        val result = cheerService.cheerAnimal(1L, 100L)

        // 3. Then
        // 응원이 성공하여 남은 횟수가 4가 되어야 함 (5회 중 1회 사용)
        Assertions.assertThat(result.remaingCheersToday).isEqualTo(4)

        // 유저의 상태가 오늘 날짜로 갱신되었는지 확인
        Assertions.assertThat(testUser.dailyHeartCount).isEqualTo(1)
        Assertions.assertThat(testUser.lastHeartResetDate).isEqualTo(LocalDate.now())

        Mockito.verify(cheerRepository, Mockito.times(1))
            .save(ArgumentMatchers.any(Cheer::class.java))
    }
}