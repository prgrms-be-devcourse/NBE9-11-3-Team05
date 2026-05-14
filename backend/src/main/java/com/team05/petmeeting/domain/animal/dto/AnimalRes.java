package com.team05.petmeeting.domain.animal.dto;

import com.team05.petmeeting.domain.animal.entity.Animal;

public record AnimalRes(
        Long animalId,
        String desertionNo,
        String processState,
        String noticeNo,
        String noticeEdt,

        String upKindNm,
        String kindFullNm,
        String colorCd,
        String age,
        String weight,
        String sexCd,

        String popfile1,
        String popfile2,

        String careNm,
        String careTel,
        String careAddr,

        Integer totalCheerCount,
        double temperature,

        String care_reg_no
) {
    public AnimalRes(Animal animal) {
        this(
                animal.getId(),
                animal.getDesertionNo(),
                animal.getProcessState(),
                animal.getNoticeNo(),
                String.valueOf(animal.getNoticeEdt()),

                animal.getUpKindNm(),
                animal.getKindFullNm(),
                animal.getColorCd(),
                animal.getAge(),
                animal.getWeight(),
                animal.getSexCd(),

                animal.getPopfile1(),
                animal.getPopfile2(),

                animal.getCareNm(),
                animal.getCareTel(),
                animal.getCareAddr(),

                animal.getTotalCheerCount(),
                animal.getTotalCheerCount() / (double) 50, // 목표하트수 50 임시설정값

                animal.getShelter().getCareRegNo()
        );
    }
}
