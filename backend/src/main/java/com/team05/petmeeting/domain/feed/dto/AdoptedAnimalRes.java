package com.team05.petmeeting.domain.feed.dto;

import com.team05.petmeeting.domain.animal.entity.Animal;

public record AdoptedAnimalRes(
        Long animalId,
        String noticeNo,
        String upKindNm,    // 개/고양이
        String kindFullNm,  // 품종
        String imageUrl
) {
    public static AdoptedAnimalRes from(Animal animal) {
        return new AdoptedAnimalRes(
                animal.getId(),
                animal.getNoticeNo(),
                animal.getUpKindNm(),
                animal.getKindFullNm(),
                animal.getPopfile1()
        );
    }
}