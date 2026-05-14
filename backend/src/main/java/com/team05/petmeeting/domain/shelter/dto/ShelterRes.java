package com.team05.petmeeting.domain.shelter.dto;

import com.team05.petmeeting.domain.shelter.entity.Shelter;

public record ShelterRes (
    String shelterId,  // careRegNo
    String careNm,
    String careTel,
    String careAddr,
    String orgNm
) {
    public static ShelterRes from(Shelter shelter) {
        return new ShelterRes(
            shelter.getCareRegNo(),
            shelter.getCareNm(),
            shelter.getCareTel(),
            shelter.getCareAddr(),
            shelter.getOrgNm()
        );
    }
}
