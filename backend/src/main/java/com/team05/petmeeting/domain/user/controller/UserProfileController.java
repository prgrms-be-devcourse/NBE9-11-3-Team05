package com.team05.petmeeting.domain.user.controller;

import com.team05.petmeeting.domain.donation.service.DonationService;
import com.team05.petmeeting.domain.user.dto.profile.MyProfileDetailRes;
import com.team05.petmeeting.domain.user.dto.profile.NicknameReq;
import com.team05.petmeeting.domain.user.dto.profile.PasswordReq;
import com.team05.petmeeting.domain.user.dto.profile.ProfileImgReq;
import com.team05.petmeeting.domain.user.dto.profile.UserAnimalCommentRes;
import com.team05.petmeeting.domain.user.dto.profile.UserCheerAnimalRes;
import com.team05.petmeeting.domain.user.dto.profile.UserDonationRes;
import com.team05.petmeeting.domain.user.dto.profile.UserFeedCommentRes;
import com.team05.petmeeting.domain.user.dto.profile.UserFeedRes;
import com.team05.petmeeting.domain.user.dto.profile.UserProfileRes;
import com.team05.petmeeting.domain.user.dto.profile.UserSummaryRes;
import com.team05.petmeeting.domain.user.service.UserProfileService;
import com.team05.petmeeting.global.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@Validated
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final DonationService donationService;

    // 닉네임 변경
    @PatchMapping("/nickname")
    public ResponseEntity<UserProfileRes> nickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody NicknameReq req
    ) {
        Long userId = userDetails.getUserId();
        UserProfileRes res = userProfileService.modifyNickname(userId, req.getNickname());
        return ResponseEntity.ok(res);
    }

    // 프로필 사진 등록 & 변경
    @PatchMapping("/profileImg")
    public ResponseEntity<UserProfileRes> profileImg(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProfileImgReq req
    ) {
        Long userId = userDetails.getUserId();
        UserProfileRes res = userProfileService.modifyProfileImageUrl(userId, req.getProfileImageUrl());
        return ResponseEntity.ok(res);
    }

    // 비밀번호 변경
    @PatchMapping("/password")
    public ResponseEntity<Void> password(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordReq req
    ) {
        Long userId = userDetails.getUserId();
        userProfileService.modifyPassword(userId, req.getCurrentPassword(), req.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    // 그동안 응원 누른 응원 수 , 동물 수
    @GetMapping
    public ResponseEntity<MyProfileDetailRes> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MyProfileDetailRes res = userProfileService.getMyProfileDetails(userDetails.getUserId());
        return ResponseEntity.ok(res);
    }

    // 작성 글 목록 & 갯수 전달
    @GetMapping("/feeds")
    public ResponseEntity<UserFeedRes> feeds(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        UserFeedRes res = userProfileService.getMyFeeds(userId);
        return ResponseEntity.ok(res);
    }

    // 작성 피드 댓글 목록 & 갯수 전달
    @GetMapping("/comments/feeds")
    public ResponseEntity<UserFeedCommentRes> feedComments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        UserFeedCommentRes res = userProfileService.getMyFeedComments(userId);
        return ResponseEntity.ok(res);
    }

    // 작성 동물 댓글 목록 & 갯수 전달
    @GetMapping("/comments/animals")
    public ResponseEntity<UserAnimalCommentRes> animalComments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        UserAnimalCommentRes res = userProfileService.getMyAnimalComments(userId);
        return ResponseEntity.ok(res);
    }

    // 응원 동물 목록 & 갯수 전달
    @GetMapping("/cheer-animals")
    public ResponseEntity<UserCheerAnimalRes> animals(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        UserCheerAnimalRes res = userProfileService.getMyCheerAnimals(userId);
        return ResponseEntity.ok(res);
    }

    // 기본 프로필 정보 조회
    @GetMapping("/profile")
    public ResponseEntity<UserProfileRes> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        UserProfileRes res = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/summary")
    public ResponseEntity<UserSummaryRes> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        UserSummaryRes res = userProfileService.getUserSummary(userId);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "사용자 후원 목록 조회")
    @GetMapping("/donations")
    public ResponseEntity<UserDonationRes> getDonations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        UserDonationRes res = donationService.getMyDonations(userId);
        return ResponseEntity.ok(res);
    }

}
