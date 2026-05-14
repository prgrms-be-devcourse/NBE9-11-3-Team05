package com.team05.petmeeting.domain.user.dto.emailstart;

public record EmailStartRes(
        boolean exists,
        NextStep nextStep
) {
    public enum NextStep {
        SIGNUP_WITH_OTP, LOGIN_PASSWORD, SOCIAL_LOGIN_ONLY
    }
}
