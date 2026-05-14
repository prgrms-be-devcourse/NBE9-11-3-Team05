package com.team05.petmeeting.domain.naming.entity;

import com.team05.petmeeting.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bad_words")
public class BadWord extends BaseEntity { // 금칙어 엔티티

    @Column(nullable = false, unique = true)
    private String word; // 금칙어 단어

    public BadWord(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }
}
