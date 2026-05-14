package com.team05.petmeeting.domain.adoption.dto;

import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionReviewReq {
    private AdoptionStatus status;
    private String rejectionReason;
}
