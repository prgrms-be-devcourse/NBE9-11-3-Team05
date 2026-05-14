package com.team05.petmeeting.domain.user.dto.profile;

import com.team05.petmeeting.domain.user.entity.User;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserProfileRes(
        String profileImageUrl,
        String nickname,
        String email,
        String name,
        LocalDateTime createdAt
) {
    public static UserProfileRes from(User user) {
        return new UserProfileRes(
                user.getProfileImageUrl(),
                user.getNickname(),
                maskEmail(user.getEmail()),
                user.getRealname(),
                user.getCreatedAt()
        );
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return local.charAt(0) + "*@" + domain;
        }

        StringBuilder masked = new StringBuilder();
        masked.append(local.charAt(0));
        for (int i = 1; i < local.length() - 1; i++) {
            masked.append("*");
        }
        masked.append(local.charAt(local.length() - 1));

        return masked + "@" + domain;
    }
}
