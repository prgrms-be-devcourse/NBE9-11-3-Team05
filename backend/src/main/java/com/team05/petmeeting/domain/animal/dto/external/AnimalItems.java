package com.team05.petmeeting.domain.animal.dto.external;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AnimalItems {
    private List<AnimalItem> item;
}
