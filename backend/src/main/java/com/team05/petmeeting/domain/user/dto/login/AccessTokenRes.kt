package com.team05.petmeeting.domain.user.dto.login

data class AccessTokenRes(
    val tokenType: String,
    val accessToken: String
)
