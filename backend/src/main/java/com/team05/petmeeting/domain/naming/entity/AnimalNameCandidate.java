package com.team05.petmeeting.domain.naming.entity;

import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "animal_name_candidate",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_animal_name",
                        columnNames = {"animal_id", "proposed_name"}
                )
        }
)
public class AnimalNameCandidate extends BaseEntity { // 이름 후보 (user_id 포함)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;      // 대상 유기동물 ID

    @Column(nullable = false, length = 10)
    private String proposedName; // 제안된 이름

    @Column(nullable = false)
    private Integer voteCount = 0; // 현재 득표수

    @Column(nullable = false)
    private boolean isConfirmed = false; // 최종 선정 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 최초 작명자

    public AnimalNameCandidate(Animal animal, User proposer, String proposedName) {
        this.animal = animal;
        this.proposedName = proposedName;
        this.user = proposer;
        this.voteCount = 0; // 초기값 명시
        this.isConfirmed = false;
    }

    // 득표수 증가 메서드
    public void addVoteCount() {
        this.voteCount++;
    }

    // 이름 확정 메서드
    public void confirmName() {
        this.isConfirmed = true;
    }

    public Animal getAnimal() {
        return animal;
    }

    public String getProposedName() {
        return proposedName;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public User getUser() {
        return user;
    }
}
