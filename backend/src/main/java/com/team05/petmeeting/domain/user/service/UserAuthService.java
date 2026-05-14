package com.team05.petmeeting.domain.user.service;

import static com.team05.petmeeting.global.security.util.RefreshTokenUtil.REFRESH_TOKEN_COOKIE_NAME;

import com.team05.petmeeting.domain.user.dto.emailsignup.EmailSignupReq;
import com.team05.petmeeting.domain.user.dto.emailstart.EmailStartRes;
import com.team05.petmeeting.domain.user.dto.emailstart.EmailStartRes.NextStep;
import com.team05.petmeeting.domain.user.dto.login.AccessTokenRes;
import com.team05.petmeeting.domain.user.dto.login.LoginAndRefreshRes;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.entity.UserAuth;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.domain.user.provider.Provider;
import com.team05.petmeeting.domain.user.refreshtoken.entity.RefreshToken;
import com.team05.petmeeting.domain.user.refreshtoken.repository.RefreshTokenRepository;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import com.team05.petmeeting.global.security.errorCode.SecurityErrorCode;
import com.team05.petmeeting.global.security.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserAuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MailService mailService;
    private final OtpService otpService;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public EmailStartRes startEmailFlow(String email) {

        return userRepository.findByEmailWithAuths(email)
                .map(user -> user.getUserAuths().stream()
                        .anyMatch(auth -> auth.getProvider() == Provider.LOCAL)
                        ? new EmailStartRes(true, NextStep.LOGIN_PASSWORD)
                        : new EmailStartRes(true, NextStep.SOCIAL_LOGIN_ONLY)
                )
                .orElseGet(() -> {
                    return new EmailStartRes(false, NextStep.SIGNUP_WITH_OTP);
                });
    }

    public void sendSignupOtp(String email) {

        // rate limit 체크
        otpService.checkCooldown(email);

        String code = otpService.saveSignupOtp(email);

        mailService.sendMail(email, code);
    }

    public String verifyOtp(String email, String code) {

        Optional<String> savedCode = otpService.getSignupOtp(email);

        // 1. OTP 없음 (만료 or 없음)
        if (savedCode.isEmpty()) {
            throw new BusinessException(UserErrorCode.EXPIRED_OTP);
        }

        // 2. 코드 불일치
        if (!savedCode.get().equals(code)) {
            otpService.increaseAttempt(email);

            if (otpService.isExceededAttempts(email)) {
                otpService.clearOtp(email);
                throw new BusinessException(UserErrorCode.TOO_MANY_OTP_ATTEMPTS);
            }

            throw new BusinessException(UserErrorCode.INVALID_OTP);
        }

        // 3. 성공
        otpService.clearOtp(email);
        return otpService.markVerifiedWithToken(email);
    }

    public LoginAndRefreshRes signupAndLoginWithEmail(EmailSignupReq request) {

        // verification token으로 email 조회
        String email = otpService.getEmailByVerifyToken(request.getVerificationToken())
                .orElseThrow(() -> new BusinessException(UserErrorCode.INVALID_VERIFICATION_TOKEN));

        // 이미 가입된 이메일인지 체크
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(UserErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 생성
        User user = User.create(
                email,
                request.getNickname(),
                request.getRealname()
        );

        // UserAuth 추가 (LOCAL)
        user.addAuth(
                UserAuth.create(Provider.LOCAL, email, encodedPassword)
        );

        User savedUser = userRepository.save(user);

        // 6. verification token 제거 (재사용 방지)
        otpService.clearVerifiedByToken(request.getVerificationToken());

        return issueToken(savedUser);
    }

    public LoginAndRefreshRes loginWithEmail(String email, String password) {
        User user = userRepository.findByEmailWithAuths(email)
                .orElseThrow(() -> new BusinessException(UserErrorCode.LOGIN_FAILED));

        UserAuth auth = user.getUserAuths().stream()
                .filter(a -> a.getProvider() == Provider.LOCAL)
                .findFirst()
                .orElseThrow(() -> new BusinessException(UserErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(password, auth.getPassword())) {
            throw new BusinessException(UserErrorCode.LOGIN_FAILED);
        }

        return issueToken(user);
    }

    public void logout(HttpServletRequest request) {
        log.info("------------------------- logout start -------------------------");
        extractRefreshToken(request) // Cookie로부터 refreshToken 추출
                .ifPresent(token -> {
                    UUID uuid = UUID.fromString(token);
                    refreshTokenRepository.deleteByToken(uuid); // db에서 리프레시 토큰 삭제
                });
    }

    public LoginAndRefreshRes refresh(HttpServletRequest request) {

        // 1. 쿠키에서 refreshToken 추출
        String refreshToken = extractRefreshToken(request)
                .orElseThrow(() -> new BusinessException(SecurityErrorCode.INVALID_TOKEN)
                );

        // 2. DB 조회
        RefreshToken savedToken = refreshTokenRepository.findByToken(UUID.fromString(refreshToken))
                .orElseThrow(() -> new BusinessException(SecurityErrorCode.INVALID_TOKEN));

        // 리프레시 토큰의 유효 기간이 지났을 경우 예외 throw
        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(savedToken); // cleanup
            throw new BusinessException(SecurityErrorCode.INVALID_TOKEN);
        }

        User user = savedToken.getUser();

        // 3. access token 재발급
        String newAccessToken = jwtUtil.createToken(user.getId(), List.of(user.getRole().name()));

        // 4. refresh token rotate
        refreshTokenRepository.delete(savedToken);

        UUID uuid = UUID.randomUUID();
        RefreshToken saved = RefreshToken.create(user, uuid);
        refreshTokenRepository.save(saved);

        return new LoginAndRefreshRes(
                uuid.toString(),
                new AccessTokenRes("Bearer", newAccessToken)
        );
    }

    public void withdraw(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        // 1. refresh token 전체 삭제
        refreshTokenRepository.deleteAllByUser(user);

        // 2. 유저 삭제
        userRepository.delete(user);

        // todo: soft delete 추후 고려
    }

    private LoginAndRefreshRes issueToken(User user) {

        // jwt access token 생성
        String accessToken = jwtUtil.createToken(user.getId(), List.of(user.getRole().name()));

        // refresh 토큰 생성 및 db 저장 -> redis 변경 검토
        UUID uuid = UUID.randomUUID();
        RefreshToken saved = RefreshToken.create(user, uuid);
        refreshTokenRepository.save(saved);

        // dto 반환
        return new LoginAndRefreshRes(
                uuid.toString(),
                new AccessTokenRes("Bearer", accessToken)
        );
    }

    private Optional<String> extractRefreshToken(HttpServletRequest request) {

        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(REFRESH_TOKEN_COOKIE_NAME))
                .map(Cookie::getValue)
                .findFirst();
    }
}