package com.team05.petmeeting.domain.naming.entity;

import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "name_vote_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_candidate_vote",
                        columnNames = {"user_id", "animal_id"}
                )
        }
)
public class NameVoteHistory{ // 복합 유니크 키: 유저는 한 동물에 한번만 투표가능
    // 투표 내역 누적됨 -> 사용하지 않는 수정일은 메모리 낭비
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 투표한 유저

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal; // 어떤 동물에게 투표했는지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private AnimalNameCandidate candidate; // 투표 대상 후보

    @Column(nullable = false, updatable = false) // 수정 불가능하게 설정
    private LocalDateTime votedAt;

    public NameVoteHistory(User user, Animal animal, AnimalNameCandidate candidate) {
        this.user = user;
        this.animal = animal;
        this.candidate = candidate;
        this.votedAt = LocalDateTime.now();
    }

}
