package com.team05.petmeeting.domain.feed.entity;

import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.comment.entity.FeedComment;
import com.team05.petmeeting.domain.feed.enums.FeedCategory;
import com.team05.petmeeting.domain.feed.errorCode.FeedErrorCode;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.global.entity.BaseEntity;
import com.team05.petmeeting.global.exception.BusinessException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feeds")
public class Feed extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;    // FK

    @ManyToOne(fetch = FetchType.LAZY)
    private Animal animal; // FK

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FeedCategory category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FeedComment> comments = new ArrayList<>();

    public Feed(User user, FeedCategory category, String title, String content, String imageUrl, Animal animal) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.animal = animal;
    }

    public void update(FeedCategory category, String title, String content, String imageUrl) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    // 테스트용 (user 없이)
    public Feed(FeedCategory category, String title, String content, String imageUrl) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public void checkModify(User user) {
        if (!this.user.getId().equals(user.getId())) {
            throw new BusinessException(FeedErrorCode.FORBIDDEN);
        }
    }

    public void checkDelete(User user) {
        if (!this.user.getId().equals(user.getId())) {
            throw new BusinessException(FeedErrorCode.FORBIDDEN);
        }
    }

}
