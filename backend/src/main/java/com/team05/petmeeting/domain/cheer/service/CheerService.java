package com.team05.petmeeting.domain.cheer.service;

import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.animal.errorCode.AnimalErrorCode;
import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import com.team05.petmeeting.domain.cheer.dto.CheerRes;
import com.team05.petmeeting.domain.cheer.dto.CheerStatusDto;
import com.team05.petmeeting.domain.cheer.entity.Cheer;
import com.team05.petmeeting.domain.cheer.errorCode.CheerErrorCode;
import com.team05.petmeeting.domain.cheer.repository.CheerRepository;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CheerService {

    private final CheerRepository cheerRepository;
    private final UserRepository userRepository;
    private final AnimalRepository animalRepository;

    // 오늘 응원 상태 조회
    public CheerStatusDto getTodaysStatus(long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(UserErrorCode.USER_NOT_FOUND)
        );

        // 초기화 필요하면 응원 개수 초기화
        user.resetDailyHeartCountIfNeeded();

        int usedToday = user.getDailyHeartCount();
        int remainingToday = 5 - usedToday;

        // 내일 자정 계산 (DB에 저장하지 않고, 매번 계산해서 사용)
        LocalDateTime tomorrow_midnight = LocalDate.now().plusDays(1) // 2026-04-13
                .atStartOfDay();       // 00:00:00
        String resetAt = tomorrow_midnight.toString();

        return new CheerStatusDto(usedToday, remainingToday, resetAt);
    }

    // 응원 부여
    public CheerRes cheerAnimal(long userId, long animalId) {
        // 사용자 조회
        User user = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException(UserErrorCode.USER_NOT_FOUND)
        );
        // 동물 조회
        Animal animal = animalRepository.findById(animalId).orElseThrow(
                () -> new BusinessException(AnimalErrorCode.ANIMAL_NOT_FOUND)
        );

        // 초기화 필요하면 응원 개수 초기화
        user.resetDailyHeartCountIfNeeded();

        // 5회 제한 확인
        if (user.getDailyHeartCount() >= 5) {
            throw new BusinessException(CheerErrorCode.DAILY_CHEER_LIMIT_EXCEEDED);
        }

        // cheer 객체 생성 & 저장
        Cheer cheer = new Cheer(user, animal);
        cheerRepository.save(cheer);
        // user 응원 횟수 증가
        user.useDailyCheer();
        userRepository.saveAndFlush(user); // 변경된 user 상태를 DB에 즉시 반영

        animalRepository.incrementCheerCount(animalId); // 원자적 업데이트(동시성 안전), 캐시 비워짐

        // 캐시가 비워졌으므로 DB에서 업데이트된 Animal, User 가져오기
        Animal updatedAnimal = animalRepository.findById(animalId).get();
        User updatedUser = userRepository.findById(userId).get();

        return new CheerRes(
                animalId,
                updatedAnimal.getTotalCheerCount(),
                updatedAnimal.getTemperature(),
                5 - updatedUser.getDailyHeartCount()
        );
    }

}