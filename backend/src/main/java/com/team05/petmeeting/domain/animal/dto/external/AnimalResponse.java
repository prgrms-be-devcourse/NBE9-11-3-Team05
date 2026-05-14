package com.team05.petmeeting.domain.animal.dto.external;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnimalResponse {
    private AnimalHeader header;
    private AnimalBody body;
}
