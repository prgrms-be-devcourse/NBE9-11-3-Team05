package com.team05.petmeeting.global.initData;

import com.team05.petmeeting.domain.animal.repository.AnimalRepository;
import com.team05.petmeeting.domain.animal.service.AnimalSyncService;
import com.team05.petmeeting.domain.campaign.dto.CampaignCreateReq;
import com.team05.petmeeting.domain.campaign.service.CampaignService;
import com.team05.petmeeting.domain.comment.dto.CommentReq;
import com.team05.petmeeting.domain.comment.service.CommentService;
import com.team05.petmeeting.domain.feed.dto.FeedReq;
import com.team05.petmeeting.domain.feed.dto.FeedRes;
import com.team05.petmeeting.domain.feed.enums.FeedCategory;
import com.team05.petmeeting.domain.feed.service.FeedService;
import com.team05.petmeeting.domain.shelter.dto.ShelterCommand;
import com.team05.petmeeting.domain.shelter.entity.Shelter;
import com.team05.petmeeting.domain.shelter.repository.ShelterRepository;
import com.team05.petmeeting.domain.shelter.service.ShelterService;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.entity.UserAuth;
import com.team05.petmeeting.domain.user.provider.Provider;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.domain.user.service.UserService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Profile("!test")  // test 환경에서 안 돌아가게 막음
public class BaseInitData {

    @Autowired
    @Lazy
    private BaseInitData self;
    private final FeedService feedService;
    private final CommentService commentService;
    private final AnimalSyncService animalSyncService;
    private final UserRepository userRepository;
    private final ShelterRepository shelterRepository;
    private final ShelterService shelterService;
    private final AnimalRepository animalRepository;
    private final PasswordEncoder passwordEncoder;
    private final CampaignService campaignService;
    private final UserService userService;


    @Bean
    public ApplicationRunner initData() {
        return args -> {
            self.work1();
            self.work2();
            self.work3();
        };
    }

    // 유저, 피드, 댓글 생성
    @Transactional
    public void work1() {
        if (userRepository.count() > 0) {
            return;
        }
        User user = userRepository.save(
                User.create("admin@naver.com", "admin_nickname", "홍길동")
        );
        UserAuth userAuth = UserAuth.create(Provider.LOCAL, "admin@naver.com", passwordEncoder.encode("12345678Aa!"));
        FeedRes res1 = feedService.write(new FeedReq(FeedCategory.FREE, "제목1", "내용1", null, null), user);
        FeedRes res2 = feedService.write(new FeedReq(FeedCategory.FREE, "제목2", "내용2", null, null), user);
        commentService.createFeedComment(user.getId(), res1.getFeedId(), new CommentReq("댓글1"));

    }

    // 동물 외부 api 호출
    public void work2() {
        if (animalRepository.count() > 0) {
            return;
        }
        animalSyncService.fetchAndSaveAnimals(1, 30);
    }

    // 보호소, 캠페인, 도네이션 생성
    @Transactional
    public void work3() {
        if (shelterRepository.count() > 0) {
            return;
        }
        Shelter shelter = shelterService.createOrUpdateShelter(
                new ShelterCommand("343447202600001", "음성군 동물보호센터", "043-877-3081", "충청북도 음성군 삼성면 대금로 715-5", "음성군수",
                        "충청북도 음성군", LocalDateTime.now()));
        User user = userService.findById(1L);
        shelter.assignUser(user);
        campaignService.createCampaign(shelter.getCareRegNo(), user.getId(),
                new CampaignCreateReq("예시 캠페인", "description", 1000000));
    }
}
