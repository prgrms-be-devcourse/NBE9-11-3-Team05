package com.team05.petmeeting.domain.donation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team05.petmeeting.domain.donation.dto.CompleteReq;
import com.team05.petmeeting.domain.donation.dto.CompleteRes;
import com.team05.petmeeting.domain.donation.dto.PrepareReq;
import com.team05.petmeeting.domain.donation.dto.PrepareRes;
import com.team05.petmeeting.domain.donation.enums.DonationStatus;
import com.team05.petmeeting.domain.donation.service.DonationService;
import com.team05.petmeeting.global.security.test.WithCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = DonationController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.team05\\.petmeeting\\.global\\.security\\..*"
        )
)
@WithCustomUser(userId = 100L)  // 이미 로그인 된 사용자 만들어주는 것. api 사용하는 건 인증 된 사용자여야 해서 필요함..
public class DonationControllerTest {

    @Autowired
    private MockMvc mvc;

    // @Autowired
    // private ObjectMapper objectMapper;
    // JacksonConfig.java   ← 이게 ObjectMapper 빈을 등록하고 있을 것
    // 근데 global/security 파일들 빈으로 로딩 안 해서 수동으로 만들어줘야 함
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DonationService donationService;

    @Test
    @DisplayName("결제 준비 성공")
    void prepareDonation_success() throws Exception {
        PrepareReq req = new PrepareReq(1L, 10000);
        PrepareRes res = new PrepareRes("payment-123", 10000);

        when(donationService.prepare(eq(100L), any(PrepareReq.class))).thenReturn(res);

        mvc.perform(post("/api/v1/donations/prepare")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("payment-123"))
                .andExpect(jsonPath("$.amount").value(10000));
    }

    @Test
    @DisplayName("결제 완료 성공")
    void completeDonation_success() throws Exception {
        CompleteReq req = new CompleteReq("payment-123");
        CompleteRes res = new CompleteRes(1L, 10000, DonationStatus.PAID, 1L);

        when(donationService.donate(eq(100L), any(CompleteReq.class))).thenReturn(res);

        mvc.perform(post("/api/v1/donations/complete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(10000))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.campaignId").value(1L));
    }
}
