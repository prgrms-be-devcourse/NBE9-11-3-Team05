package com.team05.petmeeting.domain.cheer.dto;

public record CheerRes(
        Long animalId,
        int cheerCount,         // 응원 후 총 응원수
        double temperature,     // 응원 후 온도
        int remaingCheersToday  // 응원 후 오늘 남은 응원 횟수
) {

}
