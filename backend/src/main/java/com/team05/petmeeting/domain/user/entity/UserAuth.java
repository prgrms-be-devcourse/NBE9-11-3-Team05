package com.team05.petmeeting.domain.user.entity;

import com.team05.petmeeting.domain.user.provider.Provider;
import com.team05.petmeeting.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        name = "user_auths",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_auths_provider_provider_id",
                        columnNames = {"provider", "providerId"}
                )
        }
)
public class UserAuth extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider; // LOCAL , GOOGLE , NAVER

    @Column(nullable = false)
    private String providerId; // email or provider에서 제공하는 고유 id

    @Column(nullable = true)
    private String password;

    void setUser(User user) {
        this.user = user;
    }

    public static UserAuth create(Provider provider, String providerId, String password) {
        UserAuth userAuth = new UserAuth();

        userAuth.provider = provider;
        userAuth.providerId = providerId;
        userAuth.password = password;

        return userAuth;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
