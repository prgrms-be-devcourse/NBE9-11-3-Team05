package com.team05.petmeeting.domain.user.dto.emailverify;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailVerifyReq(
        @Email
        @NotBlank
        String email,

        @Pattern(regexp = "\\d{6}", message = "인증 코드는 6자리 숫자여야 합니다.")
        String code
) {
}
