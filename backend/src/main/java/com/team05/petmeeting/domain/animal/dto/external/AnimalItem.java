package com.team05.petmeeting.domain.animal.dto.external;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnimalItem {
    private String desertionNo;
    private String processState;
    private String noticeNo;
    private String noticeEdt;

    private String happenPlace;
    private String upKindNm;
    private String kindFullNm;
    private String colorCd;
    private String age;
    private String weight;
    private String sexCd;

    private String popfile1;
    private String popfile2;

    private String specialMark;
    private String careOwnerNm;
    private String careNm;
    private String careAddr;
    private String careTel;
    private String updTm; // API에서 제공하는 데이터의 최종 업데이트 시각

    private String careRegNo;
    private String orgNm;
}
