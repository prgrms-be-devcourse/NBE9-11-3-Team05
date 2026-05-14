package com.team05.petmeeting.domain.shelter.entity;

import com.team05.petmeeting.domain.campaign.entity.Campaign;
import com.team05.petmeeting.domain.shelter.dto.ShelterCommand;
import com.team05.petmeeting.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "shelters")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Shelter {

    @Id
    @Column(name = "care_reg_no")
    private String careRegNo;  // primary key, from 외부 api

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = true)
    private User user;

    @Column(name = "care_nm")
    private String careNm;

    @Column(name="care_tel")
    private String careTel;

    @Column(name = "care_addr")
    private String careAddr;

    @Column(name = "care_owner_nm")
    private String careOwnerNm;

    @Column(name = "org_nm")
    private String orgNm;

    @Column(name = "upd_tm")
    private LocalDateTime updTm;  // 외부 api 업데이트 시간

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "shelter", fetch = FetchType.LAZY)
    private List<Campaign> campaigns = new ArrayList<>();

    @Builder (access = AccessLevel.PRIVATE)
    private Shelter(String careRegNo, String careNm,
                   String careTel, String careAddr,
                   String careOwnerNm, String orgNm, LocalDateTime updTm) {
        this.careRegNo = careRegNo;
        this.careNm = careNm;
        this.careTel = careTel;
        this.careAddr = careAddr;
        this.careOwnerNm = careOwnerNm;
        this.orgNm = orgNm;
        this.updTm = updTm;
    }

    public static Shelter create(ShelterCommand cmd) {
        return new Shelter(
                cmd.careRegNo(),
                cmd.careNm(),
                cmd.careTel(),
                cmd.careAddr(),
                cmd.careOwnerNm(),
                cmd.orgNm(),
                cmd.updTm()
        );
    }

    public void updateFrom(ShelterCommand cmd) {
        this.careNm = cmd.careNm();
        this.careTel = cmd.careTel();
        this.careAddr = cmd.careAddr();
        this.careOwnerNm = cmd.careOwnerNm();
        this.orgNm = cmd.orgNm();
        this.updTm = cmd.updTm();
    }

    public void assignUser(User user){
        this.user = user;
    }

    public boolean isManagedBy(Long userId){
        return this.user != null && this.user.getId().equals(userId);
    }
}
