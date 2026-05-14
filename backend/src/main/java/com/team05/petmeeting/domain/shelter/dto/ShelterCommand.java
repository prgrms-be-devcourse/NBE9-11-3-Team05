package com.team05.petmeeting.domain.shelter.dto;

import java.time.LocalDateTime;

public record ShelterCommand(
    String careRegNo,
    String careNm,
    String careTel,
    String careAddr,
    String careOwnerNm,
    String orgNm,
    LocalDateTime updTm
) {
}
