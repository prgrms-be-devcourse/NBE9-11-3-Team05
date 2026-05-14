package com.team05.petmeeting.domain.campaign.service;

import com.team05.petmeeting.domain.animal.service.AnimalExternalService;
import com.team05.petmeeting.domain.campaign.dto.CampaignCreateReq;
import com.team05.petmeeting.domain.campaign.dto.CampaignCreateRes;
import com.team05.petmeeting.domain.campaign.errorCode.CampaignErrorCode;
import com.team05.petmeeting.domain.campaign.repository.CampaignRepository;
import com.team05.petmeeting.domain.shelter.dto.ShelterCommand;
import com.team05.petmeeting.domain.shelter.entity.Shelter;
import com.team05.petmeeting.domain.shelter.repository.ShelterRepository;
import com.team05.petmeeting.domain.shelter.service.ShelterService;
import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class CampaignServiceTest {
    @MockitoBean
    AnimalExternalService animalExternalService;  // 외부 의존 mock 처리

    @Autowired
    CampaignService campaignService;
    @Autowired CampaignRepository campaignRepository;
    @Autowired ShelterService shelterService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private ShelterRepository shelterRepository;

    private Long userId;
    private Long otherUserId;
    private String shelterId;

//    @Test
//    @DisplayName("캠페인 생성 성공")
//    public void createCampaign() {
//        // given - User 먼저 만들기
//        User user = userRepository.save(User.create("test@test.com", "nickname", "realname"));
//
//        // 보호소에 유저 할당
//        ShelterCommand cmd = new ShelterCommand(
//                "123", "보호소1", "010", "주소", "소유자", "기관", LocalDateTime.now()
//        );
//        shelterService.createOrUpdateShelter(cmd);
//        Shelter shelter = shelterService.findById("123");
//        shelter.assignUser(user);
//
//        // when
//        campaignService.createCampaign("123", user.getId(), new CampaignCreateReq("사료 후원", "사료 후원 설명", 1000000));
//
//        // then
//        Campaign result = campaignRepository
//                .findByShelter_CareRegNoAndStatus("123", CampaignStatus.ACTIVE)
//                .orElseThrow();
//        assertThat(result.getTitle()).isEqualTo("사료 후원");
//    }



    @BeforeEach
    void setUp() {
        // 보호소 관리자 유저
        User user = User.create("test@test.com", "테스터", "홍길동");
        user = userRepository.save(user);
        userId = user.getId();

        // 다른 유저 (권한 없음)
        User otherUser = User.create("other@test.com", "다른유저", "김철수");
        otherUser = userRepository.save(otherUser);
        otherUserId = otherUser.getId();

        // 보호소 생성 및 유저 연결
        ShelterCommand cmd = new ShelterCommand(
                "shelter-001",
                "테스트보호소",
                "010-0000-0000",
                "서울시 테스트구",
                "홍길동",
                "테스트기관",
                LocalDateTime.now()
        );
        Shelter shelter = Shelter.create(cmd);
        shelter.assignUser(user);
        shelter = shelterRepository.save(shelter);
        shelterId = shelter.getCareRegNo();
    }

    // 캠페인 생성 성공
    @Test
    void createCampaign_success() {
        CampaignCreateReq req = new CampaignCreateReq("테스트 캠페인", "설명", 100000);
        CampaignCreateRes res = campaignService.createCampaign(shelterId, userId, req);

        assertThat(res.title()).isEqualTo("테스트 캠페인");
    }

    // 캠페인 생성 실패 - 권한 없는 유저 (CA-004)
    @Test
    void createCampaign_fail_unauthorized() {
        CampaignCreateReq req = new CampaignCreateReq("테스트 캠페인", "설명", 100000);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                campaignService.createCampaign(shelterId, otherUserId, req)
        );
        assertThat(ex.getErrorCode()).isEqualTo(CampaignErrorCode.UNAUTHORIZED_SHELTER);
    }

    // 캠페인 생성 실패 - 이미 진행 중인 캠페인 있음 (CA-003)
    @Test
    void createCampaign_fail_alreadyExists() {
        CampaignCreateReq req = new CampaignCreateReq("테스트 캠페인", "설명", 100000);
        campaignService.createCampaign(shelterId, userId, req);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                campaignService.createCampaign(shelterId, userId, req)
        );
        assertThat(ex.getErrorCode()).isEqualTo(CampaignErrorCode.CAMPAIGN_ALREADY_EXISTS);
    }

    // 캠페인 종료 성공
    @Test
    void closeCampaign_success() {
        CampaignCreateReq req = new CampaignCreateReq("테스트 캠페인", "설명", 100000);
        CampaignCreateRes created = campaignService.createCampaign(shelterId, userId, req);

        assertDoesNotThrow(() ->
                campaignService.closeCampaign(userId, created.id())
        );
    }

    // 캠페인 종료 실패 - 이미 마감된 캠페인 (CA-002)
    @Test
    void closeCampaign_fail_alreadyClosed() {
        CampaignCreateReq req = new CampaignCreateReq("테스트 캠페인", "설명", 100000);
        CampaignCreateRes created = campaignService.createCampaign(shelterId, userId, req);
        campaignService.closeCampaign(userId, created.id());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                campaignService.closeCampaign(userId, created.id())
        );
        assertThat(ex.getErrorCode()).isEqualTo(CampaignErrorCode.CAMPAIGN_CLOSED);
    }

    // 캠페인 종료 실패 - 권한 없는 유저 (CA-004)
    @Test
    void closeCampaign_fail_unauthorized() {
        CampaignCreateReq req = new CampaignCreateReq("테스트 캠페인", "설명", 100000);
        CampaignCreateRes created = campaignService.createCampaign(shelterId, userId, req);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                campaignService.closeCampaign(otherUserId, created.id())
        );
        assertThat(ex.getErrorCode()).isEqualTo(CampaignErrorCode.UNAUTHORIZED_SHELTER);
    }

    // 존재하지 않는 캠페인 종료 (CA-001)
    @Test
    void closeCampaign_fail_notFound() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                campaignService.closeCampaign(userId, 999L)
        );
        assertThat(ex.getErrorCode()).isEqualTo(CampaignErrorCode.CAMPAIGN_NOT_FOUND);
    }
}
