package com.team05.petmeeting.domain.animal.dto;

public record AnimalSyncResponse(
        String message,
        int savedCount,
        long elapsedMs
) {
}
