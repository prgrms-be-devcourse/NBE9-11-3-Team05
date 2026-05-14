package com.team05.petmeeting.domain.campaign.service;

import com.team05.petmeeting.domain.campaign.dto.CampaignCreateReq;
import com.team05.petmeeting.domain.campaign.dto.CampaignCreateRes;
import com.team05.petmeeting.domain.campaign.dto.CampaignDetailRes;
import com.team05.petmeeting.domain.campaign.dto.CampaignRes;
import com.team05.petmeeting.domain.campaign.entity.Campaign;
import com.team05.petmeeting.domain.campaign.enums.CampaignStatus;
import com.team05.petmeeting.domain.campaign.errorCode.CampaignErrorCode;
import com.team05.petmeeting.domain.campaign.repository.CampaignRepository;
import com.team05.petmeeting.domain.shelter.entity.Shelter;
import com.team05.petmeeting.domain.shelter.service.ShelterService;
import com.team05.petmeeting.global.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final ShelterService shelterService;

    public CampaignCreateRes createCampaign(String shelterId, Long userId, CampaignCreateReq req){
        Shelter shelter = shelterService.findById(shelterId);
        if (!shelter.isManagedBy(userId)) {
            throw new BusinessException(CampaignErrorCode.UNAUTHORIZED_SHELTER);
        }

        if (campaignRepository.existsByShelter_CareRegNoAndStatus(shelterId, CampaignStatus.ACTIVE)) {
            throw new BusinessException(CampaignErrorCode.CAMPAIGN_ALREADY_EXISTS);
        }

        Campaign campaign = Campaign.create(shelter, req.title(), req.description(), req.amount());
        campaignRepository.save(campaign);
        return CampaignCreateRes.from(campaign);
    }

    public CampaignDetailRes getCampaign(String shelterId){
        Shelter shelter = shelterService.findById(shelterId);
        List<Campaign> campaignList = campaignRepository.findByShelterOrderByStatusNative(shelter);
        return CampaignDetailRes.from(campaignList);
    }

    public void closeCampaign(Long userId, Long id){
        Campaign campaign = getCampaignOrThrow(userId, id);
        if (campaign.getStatus() != CampaignStatus.ACTIVE){
            throw new BusinessException(CampaignErrorCode.CAMPAIGN_CLOSED);
        }
        campaign.close();
    }

    private Campaign getCampaignOrThrow(Long userId, Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new BusinessException(CampaignErrorCode.CAMPAIGN_NOT_FOUND));
        if (!campaign.getShelter().isManagedBy(userId)) {
            throw new BusinessException(CampaignErrorCode.UNAUTHORIZED_SHELTER);
        }
        return campaign;
    }

    public CampaignRes getAllCampaigns() {
        List<Campaign> campaigns = campaignRepository.findAll();
        return CampaignRes.of(campaigns.size(), campaigns);
    }

    public Campaign findById(Long aLong) {
        return campaignRepository.findById(aLong).orElseThrow(
                () -> new BusinessException(CampaignErrorCode.CAMPAIGN_NOT_FOUND)
        );
    }
}
