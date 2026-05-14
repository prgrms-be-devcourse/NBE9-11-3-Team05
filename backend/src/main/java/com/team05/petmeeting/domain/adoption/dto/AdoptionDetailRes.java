package com.team05.petmeeting.domain.adoption.dto;

import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdoptionDetailRes {
    private Long applicationId;
    private AdoptionStatus status;
    private String applyReason;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
    private String applyTel;
    private AnimalInfo animalInfo;

    public AdoptionDetailRes(
            Long applicationId,
            AdoptionStatus status,
            String applyReason,
            LocalDateTime createdAt,
            LocalDateTime reviewedAt,
            String rejectionReason,
            String applyTel,
            AnimalInfo animalInfo
    ) {
        this.applicationId = applicationId;
        this.status = status;
        this.applyReason = applyReason;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
        this.applyTel = applyTel;
        this.animalInfo = animalInfo;
    }

    @Getter
    public static class AnimalInfo {
        private String desertionNo;
        private String specialMark; // 사진 URL
        private String careNm; // 보호소 이름
        private String careOwnerNm; // 보호소 담당자
        private String careTel; // 보호소 전화번호
        private String careAddr; // 보호소 주소

        public AnimalInfo(
                String desertionNo,
                String specialMark,
                String careNm,
                String careOwnerNm,
                String careTel,
                String careAddr
        ) {
            this.desertionNo = desertionNo;
            this.specialMark = specialMark;
            this.careNm = careNm;
            this.careOwnerNm = careOwnerNm;
            this.careTel = careTel;
            this.careAddr = careAddr;
        }
    }
}
