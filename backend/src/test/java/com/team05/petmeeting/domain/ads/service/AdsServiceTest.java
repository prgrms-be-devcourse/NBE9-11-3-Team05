package com.team05.petmeeting.domain.ads.service;

import com.team05.petmeeting.domain.animal.entity.Animal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "jwt.secret=test-secret-key-for-testing-must-be-at-least-256-bits-long-enough", // JWT 임시값
        "jwt.expireMillis=3600000",
        "claude.api.key=test-claude-key", // Claude API 임시값
        "meta.ads.access-token=test-token", // Meta Ads 임시값
        "meta.ads.ad-account-id=test-account-id",
        "meta.ads.page-id=test-page-id",
        "meta.ads.api-version=v19.0"
})
@ActiveProfiles("test")
public class AdsServiceTest {

    @Autowired
    private AdsService adsService;

    @Test
    @DisplayName("Top N 동물 조회 테스트")
    void getTopAnimals() {
        // when
        List<Animal> result = adsService.getTopAnimals(3);

        // then
        assertNotNull(result);
        assertTrue(result.size() <= 3);
        System.out.println("Top 3 동물:");
        result.forEach(animal ->
                System.out.println(
                        animal.getUpKindNm() + " / " +
                                animal.getKindFullNm() + " / " +
                                "응원수: " + animal.getTotalCheerCount()
                )
        );
    }
}