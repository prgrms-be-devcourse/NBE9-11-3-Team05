package com.team05.petmeeting.domain.user.dto.emailstart;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailStartReq(
        @Email
        @NotBlank
        String email
) {
}
