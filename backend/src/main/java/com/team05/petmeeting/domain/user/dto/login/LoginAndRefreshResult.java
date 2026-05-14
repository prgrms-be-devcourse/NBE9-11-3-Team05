package com.team05.petmeeting.domain.user.dto.login;

public record LoginAndRefreshResult(
        String refreshToken,
        AccessTokenRes accessTokenRes
) {
}
