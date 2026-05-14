package com.team05.petmeeting.domain.comment.entity;

import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "animal_comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnimalComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @Column(length = 255, nullable = false)
    private String content;

    @Builder(access = AccessLevel.PRIVATE)
    private AnimalComment(User user, Animal animal, String content) {
        this.user = user;
        this.animal = animal;
        this.content = content;
    }

    public static AnimalComment create(User user, Animal animal, String content) {
        AnimalComment comment = AnimalComment.builder()
                .user(user)
                .animal(animal)
                .content(content)
                .build();

        animal.getComments().add(comment);

        return comment;
    }

    public void updateContent(String newContent) {
        this.content = newContent;
    }
}