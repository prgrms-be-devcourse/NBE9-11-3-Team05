package com.team05.petmeeting.domain.adoption.entity;

import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "adoption_applications")
public class AdoptionApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdoptionStatus status;

    @Column(name = "apply_reason", nullable = false, length = 1000)
    private String applyReason;

    @Column(name = "apply_tel", nullable = false, length = 30)
    private String applyTel;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    private AdoptionApplication(User user, Animal animal, String applyReason, String applyTel) {
        this.user = user;
        this.animal = animal;
        this.status = AdoptionStatus.Processing;
        this.applyReason = applyReason;
        this.applyTel = applyTel;
    }

    public static AdoptionApplication create(User user, Animal animal, String applyReason, String applyTel) {
        return new AdoptionApplication(user, animal, applyReason, applyTel);
    }

    public void approve() {
        this.status = AdoptionStatus.Approved;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = null;
    }

    public void reject(String rejectionReason) {
        this.status = AdoptionStatus.Rejected;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }

    public void markProcessing() {
        this.status = AdoptionStatus.Processing;
        this.reviewedAt = null;
        this.rejectionReason = null;
    }
}
