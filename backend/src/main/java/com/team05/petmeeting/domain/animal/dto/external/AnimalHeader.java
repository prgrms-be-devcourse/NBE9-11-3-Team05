package com.team05.petmeeting.domain.animal.dto.external;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnimalHeader {
    private Long reqNo;
    private String resultCode;
    private String resultMsg;
}
