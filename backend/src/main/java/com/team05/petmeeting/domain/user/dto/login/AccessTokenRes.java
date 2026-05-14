package com.team05.petmeeting.domain.user.dto.login;

public record AccessTokenRes(

        String tokenType,
        String accessToken
) {
}
