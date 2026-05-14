package com.team05.petmeeting.domain.user.service;

import com.team05.petmeeting.domain.cheer.repository.CheerRepository;
import com.team05.petmeeting.domain.comment.entity.AnimalComment;
import com.team05.petmeeting.domain.comment.entity.FeedComment;
import com.team05.petmeeting.domain.comment.repository.AnimalCommentRepository;
import com.team05.petmeeting.domain.comment.repository.FeedCommentRepository;
import com.team05.petmeeting.domain.feed.entity.Feed;
import com.team05.petmeeting.domain.feed.repository.FeedRepository;
import com.team05.petmeeting.domain.user.dto.profile.MyProfileDetailRes;
import com.team05.petmeeting.domain.user.dto.profile.UserAnimalCommentRes;
import com.team05.petmeeting.domain.user.dto.profile.UserCheerAnimalRes;
import com.team05.petmeeting.domain.user.dto.profile.UserFeedCommentRes;
import com.team05.petmeeting.domain.user.dto.profile.UserFeedRes;
import com.team05.petmeeting.domain.user.dto.profile.UserProfileRes;
import com.team05.petmeeting.domain.user.dto.profile.UserSummaryRes;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.entity.UserAuth;
import com.team05.petmeeting.domain.user.errorCode.UserErrorCode;
import com.team05.petmeeting.domain.user.provider.Provider;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FeedRepository feedRepository;
    private final CheerRepository cheerRepository;
    private final AnimalCommentRepository animalCommentRepository;
    private final FeedCommentRepository feedCommentRepository;

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    public UserProfileRes modifyProfileImageUrl(Long userId, String profileImageUrl) {
        User user = getUserById(userId);
        user.updateProfileImageUrl(profileImageUrl);
        return UserProfileRes.from(user);
    }

    public UserProfileRes modifyNickname(Long userId, String nickname) {
        User user = getUserById(userId);
        user.updateNickname(nickname);
        return UserProfileRes.from(user);
    }

    public void modifyPassword(Long userId, @NotBlank(message = "현재 비밀번호를 입력해주세요.") String currentPassword,
                               @NotBlank(message = "새 비밀번호를 입력해주세요.") String newPassword) {
        User user = getUserById(userId);
        UserAuth userAuth = user.getUserAuths().stream()
                .filter(auth -> auth.getProvider().equals(Provider.LOCAL))
                .findFirst()
                .orElseThrow(
                        () -> new BusinessException(UserErrorCode.LOCAL_NOT_FOUND)
                );
        if (!passwordEncoder.matches(currentPassword, userAuth.getPassword())) {
            throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
        }
        if (passwordEncoder.matches(newPassword, userAuth.getPassword())) {
            throw new BusinessException(UserErrorCode.SAME_AS_OLD_PASSWORD);
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        userAuth.updatePassword(encodedPassword);

    }

    public MyProfileDetailRes getMyProfileDetails(Long userId) {
        User user = getUserById(userId);
        Long feedCount = feedRepository.countByUser(user);
        Long cheerCount = cheerRepository.countDistinctAnimalByUser(user);
        Long feedCommentCount = feedCommentRepository.countFeedCommentByUser(user);
        Long animalCommentCount = animalCommentRepository.countAnimalCommentByUser(user);
        return MyProfileDetailRes.of(feedCount, cheerCount, feedCommentCount, animalCommentCount);
    }

    public UserFeedRes getMyFeeds(Long userId) {
        User user = getUserById(userId);
        List<Feed> feedList = feedRepository.findAllByUserOrderByCreatedAtDesc(user);
        return UserFeedRes.of(feedList.size(), feedList);
    }

    public UserCheerAnimalRes getMyCheerAnimals(Long userId) {
        User user = getUserById(userId);
        List<Object[]> animalCountMap = cheerRepository.findCheerCountsByUser(user);
        return UserCheerAnimalRes.from(animalCountMap);
    }

    public UserProfileRes getUserProfile(Long userId) {
        User user = getUserById(userId);
        return UserProfileRes.from(user);
    }

    public UserSummaryRes getUserSummary(Long userId) {
        User user = getUserById(userId);
        return UserSummaryRes.from(user);
    }

    public UserFeedCommentRes getMyFeedComments(Long userId) {
        User user = getUserById(userId);
        List<FeedComment> feedCommentList = feedCommentRepository.findAllByUserOrderByCreatedAtDesc(user);
        return UserFeedCommentRes.of(feedCommentList.size(), feedCommentList);
    }

    public UserAnimalCommentRes getMyAnimalComments(Long userId) {
        User user = getUserById(userId);
        List<AnimalComment> animalCommentList = animalCommentRepository.findAllByUserOrderByCreatedAtDesc(user);
        return UserAnimalCommentRes.of(animalCommentList.size(), animalCommentList);
    }
}
