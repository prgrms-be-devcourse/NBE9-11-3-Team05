package com.team05.petmeeting.domain.user.entity;

import com.team05.petmeeting.domain.shelter.entity.Shelter;
import com.team05.petmeeting.domain.user.role.Role;
import com.team05.petmeeting.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_reg_no", referencedColumnName = "care_reg_no")
    private Shelter shelter;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String realname;

    @Column(nullable = false)
    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private int dailyHeartCount;

    @Column(nullable = false)
    private LocalDate lastHeartResetDate;

    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserAuth> userAuths = new ArrayList<>();

    public static User create(
            String email,
            String nickname,
            String realname
    ) {
        User user = new User();
        user.email = email;
        user.nickname = nickname;
        user.profileImageUrl = "";
        user.realname = realname;
        user.role = Role.USER;
        user.dailyHeartCount = 0;
        user.lastHeartResetDate = LocalDate.now();
        return user;
    }

    public void addAuth(UserAuth userAuth) {
        userAuth.setUser(this);
        userAuths.add(userAuth);
    }

    // 매일 자정마다 응원 횟수 초기화
    public void resetDailyHeartCountIfNeeded() {
        if (!this.lastHeartResetDate.equals(LocalDate.now())) {
            this.dailyHeartCount = 0;
            this.lastHeartResetDate = LocalDate.now();
        }
    }

    // 응원사용
    public void useDailyCheer() {
        this.dailyHeartCount++;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

}
