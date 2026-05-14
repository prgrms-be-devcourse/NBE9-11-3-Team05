package com.team05.petmeeting.domain.user.controller;

import com.team05.petmeeting.domain.user.dto.emailsignup.EmailSignupReq;
import com.team05.petmeeting.domain.user.dto.emailstart.EmailStartReq;
import com.team05.petmeeting.domain.user.dto.emailstart.EmailStartRes;
import com.team05.petmeeting.domain.user.dto.emailverify.EmailVerifyReq;
import com.team05.petmeeting.domain.user.dto.emailverify.EmailVerifyRes;
import com.team05.petmeeting.domain.user.dto.login.AccessTokenRes;
import com.team05.petmeeting.domain.user.dto.login.LoginAndRefreshResult;
import com.team05.petmeeting.domain.user.dto.login.local.EmailLoginReq;
import com.team05.petmeeting.domain.user.service.UserAuthService;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import com.team05.petmeeting.global.security.util.RefreshTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class UserAuthController {

    private final UserAuthService userAuthService;
    private final RefreshTokenUtil refreshTokenUtil;

    @PostMapping("/email/start")
    public ResponseEntity<EmailStartRes> startEmail(
            @RequestBody @Valid EmailStartReq request
    ) {
        EmailStartRes res = userAuthService.startEmailFlow(request.email());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/email/send-otp")
    public ResponseEntity<Void> sendEmail(
            @RequestBody @Valid EmailStartReq request
    ) {
        userAuthService.sendSignupOtp(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/email/verify")
    public ResponseEntity<EmailVerifyRes> verifyEmail(
            @Valid @RequestBody EmailVerifyReq request
    ) {
        String verifyToken = userAuthService.verifyOtp(request.email(), request.code());
        return ResponseEntity.ok(new EmailVerifyRes(verifyToken));
    }

    @PostMapping("/email/signup")
    public ResponseEntity<AccessTokenRes> signupWithEmail(
            @Valid @RequestBody EmailSignupReq request,
            HttpServletResponse response
    ) {
        LoginAndRefreshResult result = userAuthService.signupAndLoginWithEmail(request);

        refreshTokenUtil.add(response, result.refreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result.accessTokenRes());
    }

    @PostMapping("/email/login")
    public ResponseEntity<AccessTokenRes> loginWithEmail(
            @Valid @RequestBody EmailLoginReq request,
            HttpServletResponse response
    ) {
        LoginAndRefreshResult result = userAuthService.loginWithEmail(
                request.email(),
                request.password()
        );

        refreshTokenUtil.add(response, result.refreshToken());

        return ResponseEntity.ok(result.accessTokenRes());
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        userAuthService.logout(request);
        refreshTokenUtil.delete(response);

        return ResponseEntity.noContent().build();
    }

    // 리프레시 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenRes> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        LoginAndRefreshResult result = userAuthService.refresh(request);

        // refresh token 재설정 (rotate)
        refreshTokenUtil.add(response, result.refreshToken());

        return ResponseEntity.ok(result.accessTokenRes());
    }

    // 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        userAuthService.withdraw(userDetails.getUserId());

        // refresh token 쿠키 제거
        refreshTokenUtil.delete(response);

        return ResponseEntity.noContent().build();
    }


}