package com.team05.petmeeting.domain.animal.dto;

public record AnimalSyncResp(
        String message,
        int savedCount,
        long elapsedMs
) {
}
