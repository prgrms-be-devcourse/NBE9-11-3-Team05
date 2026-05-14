package com.team05.petmeeting.domain.cheer.entity;

import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "cheers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cheer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id") // ERD개선 후 animal_id 로 수정
    private Animal animal;

    public Cheer(User user, Animal animal) {
        this.user = user;
        this.animal = animal;
    }

}