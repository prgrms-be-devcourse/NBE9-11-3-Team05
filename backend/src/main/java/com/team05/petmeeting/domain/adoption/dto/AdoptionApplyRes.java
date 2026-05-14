package com.team05.petmeeting.domain.adoption.dto;

import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus;
import lombok.Getter;

@Getter
public class AdoptionApplyRes {
    private Long applicationId;
    private AdoptionStatus status;
    private AnimalInfo animalInfo;

    public AdoptionApplyRes(Long applicationId, AdoptionStatus status, AnimalInfo animalInfo) {
        this.applicationId = applicationId;
        this.status = status;
        this.animalInfo = animalInfo;
    }

    @Getter
    public static class AnimalInfo {
        private String desertionNo;
        private String kindFullNm;
        private String careNm; // 보호소 이름
        private String careOwnerNm;

        public AnimalInfo(String desertionNo, String kindFullNm, String careNm, String careOwnerNm) {
            this.desertionNo = desertionNo;
            this.kindFullNm = kindFullNm;
            this.careNm = careNm;
            this.careOwnerNm = careOwnerNm;
        }
    }
}
