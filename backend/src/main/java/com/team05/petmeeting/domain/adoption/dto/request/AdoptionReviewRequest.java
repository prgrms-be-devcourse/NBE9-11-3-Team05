package com.team05.petmeeting.domain.adoption.dto.request;

import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionReviewRequest {
    private AdoptionStatus status;
    private String rejectionReason;
}
