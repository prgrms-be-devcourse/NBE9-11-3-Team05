package com.team05.petmeeting.domain.cheer.dto;

public record CheerStatusDto(
        long usedToday,
        int remainingToday,
        String resetAt      // "2024-08-10T00:00:00"
) {

}
