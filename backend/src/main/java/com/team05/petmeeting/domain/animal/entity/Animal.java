package com.team05.petmeeting.domain.animal.entity;

import com.team05.petmeeting.domain.animal.dto.external.AnimalItem;
import com.team05.petmeeting.domain.comment.entity.AnimalComment;
import com.team05.petmeeting.domain.shelter.entity.Shelter;
import com.team05.petmeeting.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Table(name = "animals")
@NoArgsConstructor
@Builder //
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더를 위해 모든 필드 생성자 필요
public class Animal extends BaseEntity {

    @Column(name = "desertion_no", nullable = false, length = 50, unique = true)
    private String desertionNo; // 유기번호

    @Column(name = "process_state", nullable = true, length = 30)
    private String processState; // 상태 (보호중, 입양가능, 입양대기, 파양, 종결 등)

    @Column(name = "state_group", nullable = false)
    private Integer stateGroup; // 0: 보호중, 1: 종료

    @Column(name = "notice_no", nullable = true, length = 50)
    private String noticeNo; // 공고번호

    @Column(name = "notice_edt", nullable = true)
    private LocalDate noticeEdt; // 공고 종료일

    @Column(name = "happen_place", nullable = true, length = 255)
    private String happenPlace; // 발견 장소

    @Column(name = "up_kind_nm", nullable = true, length = 30)
    private String upKindNm; // 종 (개, 고양이 등)

    @Column(name = "kind_full_name", nullable = true, length = 100)
    private String kindFullNm; // 품종 (예: 믹스견, 시바견 등)

    @Column(name = "color_cd", nullable = true, length = 100)
    private String colorCd; // 색상

    @Column(name = "age", nullable = true)
    private String age; // 나이

    @Column(name = "weight", nullable = true, length = 30)
    private String weight; // 몸무게 (예: 5kg, 10kg 등)

    @Column(name = "sex_cd", nullable = true, length = 10)
    private String sexCd; // 성별

    @Column(name = "popfile1", nullable = true, length = 500)
    private String popfile1; // 사진 URL

    @Column(name = "popfile2", nullable = true, length = 500)
    private String popfile2; // 사진 URL

    @Column(name = "special_mark", nullable = true, length = 500)
    private String specialMark; // 사진 URL

    @Column(name = "care_nm", nullable = true)
    private String careNm; // 보호소 이름

    @Column(name = "care_owner_nm", nullable = true, length = 100)
    private String careOwnerNm; // 보호소 담당자

    @Column(name = "care_tel", nullable = true)
    private String careTel; // 보호소 전화번호

    @Column(name = "care_addr", nullable = true)
    private String careAddr; // 보호소 주소

    @Column(name = "total_cheer_count", nullable = false)
    private Integer totalCheerCount; // 응원 수

    @Column(name = "api_updated_at")
    private LocalDateTime apiUpdatedAt;

    @Column(name = "name", nullable = true)
    private String name;

    @OneToMany(mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnimalComment> comments = new ArrayList<>();

    @ManyToOne
    private Shelter shelter;

    private Integer determineStateGroup(String processState) {
        if (processState != null && processState.contains("보호")) {
            return 0;
        }
        return 1;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateFrom(AnimalItem item) {
        this.processState = item.getProcessState();
        this.stateGroup = determineStateGroup(item.getProcessState());
        this.noticeNo = item.getNoticeNo();
        this.noticeEdt = parseNoticeEdt(item.getNoticeEdt());
        this.happenPlace = item.getHappenPlace();
        this.upKindNm = item.getUpKindNm();
        this.kindFullNm = item.getKindFullNm();
        this.colorCd = item.getColorCd();
        this.age = item.getAge();
        this.weight = item.getWeight();
        this.sexCd = item.getSexCd();
        this.popfile1 = item.getPopfile1();
        this.popfile2 = item.getPopfile2();
        this.specialMark = item.getSpecialMark();
        this.careNm = item.getCareNm();
        this.careOwnerNm = item.getCareOwnerNm();
        this.careTel = item.getCareTel();
        this.careAddr = item.getCareAddr();
        this.apiUpdatedAt = parseUpdTm(item.getUpdTm());
    }

    public boolean needsUpdateFrom(AnimalItem item) {
        LocalDateTime incomingUpdatedAt = parseUpdTm(item.getUpdTm());

        if (this.apiUpdatedAt != null && incomingUpdatedAt != null) {
            return incomingUpdatedAt.isAfter(this.apiUpdatedAt);
        }

        return !Objects.equals(this.processState, item.getProcessState())
                || !Objects.equals(this.noticeNo, item.getNoticeNo())
                || !Objects.equals(this.happenPlace, item.getHappenPlace())
                || !Objects.equals(this.specialMark, item.getSpecialMark())
                || !Objects.equals(this.careNm, item.getCareNm());
    }

    public void assignShelter(Shelter shelter) {
        this.shelter = shelter;
    }

    private static LocalDateTime parseUpdTm(String updTm) {
        if (updTm == null || updTm.isBlank()) {
            return null;
        }

        return LocalDateTime.parse(
                updTm,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
        );
    }

    private Animal(
            String desertionNo,
            String processState,
            String noticeNo,
            LocalDate noticeEdt,
            String happenPlace,
            String upKindNm,
            String kindFullName,
            String colorCd,
            String age,
            String weight,
            String sexCd,
            String popfile1,
            String popfile2,
            String specialMark,
            String careOwnerNm,
            String careNm,
            String careTel,
            String careAddr,
            Integer totalCheerCount
    ) {
        this.desertionNo = desertionNo;
        this.processState = processState;
        this.stateGroup = determineStateGroup(processState); // 생성 시점에 0 또는 1 할당
        this.noticeNo = noticeNo;
        this.noticeEdt = noticeEdt;
        this.happenPlace = happenPlace;
        this.upKindNm = upKindNm;
        this.kindFullNm = kindFullName;
        this.colorCd = colorCd;
        this.age = age;
        this.weight = weight;
        this.sexCd = sexCd;
        this.popfile1 = popfile1;
        this.popfile2 = popfile2;
        this.specialMark = specialMark;
        this.careOwnerNm = careOwnerNm;
        this.careNm = careNm;
        this.careTel = careTel;
        this.careAddr = careAddr;
        this.totalCheerCount = totalCheerCount;
    }


    public static Animal from(AnimalItem item) {
        Animal animal = new Animal(
                item.getDesertionNo(),
                item.getProcessState(),
                item.getNoticeNo(),
                parseNoticeEdt(item.getNoticeEdt()),
                item.getHappenPlace(),
                item.getUpKindNm(),
                item.getKindFullNm(),
                item.getColorCd(),
                item.getAge(),
                item.getWeight(),
                item.getSexCd(),
                item.getPopfile1(),
                item.getPopfile2(),
                item.getSpecialMark(),
                item.getCareOwnerNm(),
                item.getCareNm(),
                item.getCareTel(),
                item.getCareAddr(),
                0
        );

        animal.apiUpdatedAt = parseUpdTm(item.getUpdTm());
        return animal;
    }

    private static LocalDate parseNoticeEdt(String noticeEdt) {
        if (noticeEdt == null || noticeEdt.isBlank()) {
            return null;
        }

        return LocalDate.parse(noticeEdt, DateTimeFormatter.BASIC_ISO_DATE);
    }

    public double getTemperature() {
        double cheerGoal = 50.0; // todo 통합 후 수정
        if (this.totalCheerCount == null) {
            return 0;
        }
        return this.totalCheerCount / cheerGoal * 100;
    }
}
