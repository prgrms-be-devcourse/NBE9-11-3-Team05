package com.team05.petmeeting.global.security.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.entity.UserAuth;
import com.team05.petmeeting.domain.user.provider.Provider;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.security.userdetails.CustomOAuth2User;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private OAuth2User mockOAuthUser(String email, String sub, String name) {
        return new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "email", email,
                        "sub", sub,
                        "name", name
                ),
                "email"
        );
    }

    @Test
    @DisplayName("신규 유저 - User + GoogleAuth 생성")
    void 신규_유저_생성() {
        // given
        String email = "test@gmail.com";
        String sub = "google-123";
        String name = "tester";

        OAuth2User oauthUser = mockOAuthUser(email, sub, name);
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);

        when(userRepository.findByEmailWithAuths(email)).thenReturn(null);

        // super.loadUser 우회
        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doReturn(oauthUser).when(spyService).getOAuthUser(any());

        // when
        OAuth2User result = spyService.loadUser(request);

        // then
        verify(userRepository).save(any(User.class));
        assertThat(result).isNotNull();
        assertThat(((CustomOAuth2User) result).getUser()).isNotNull();
    }

    @Test
    @DisplayName("기존 유저 + GoogleAuth 없음 → GoogleAuth 추가")
    void 기존유저_구글연동없음() {
        // given
        String email = "test@gmail.com";
        String sub = "google-123";
        String name = "tester";

        User user = User.create(email, "nick", name);
        user.addAuth(UserAuth.create(Provider.LOCAL, email, "pw"));

        OAuth2User oauthUser = mockOAuthUser(email, sub, name);
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);

        when(userRepository.findByEmailWithAuths(email)).thenReturn(user);

        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doReturn(oauthUser).when(spyService).getOAuthUser(any());

        // when
        spyService.loadUser(request);

        // then
        assertThat(user.getUserAuths())
                .anyMatch(auth -> auth.getProvider() == Provider.GOOGLE);
    }

    @Test
    @DisplayName("기존 유저 + GoogleAuth 있음 → 추가 안함")
    void 기존유저_구글연동있음() {
        // given
        String email = "test@gmail.com";
        String sub = "google-123";
        String name = "tester";

        User user = User.create(email, "nick", name);
        user.addAuth(UserAuth.create(Provider.GOOGLE, sub, null));

        int beforeSize = user.getUserAuths().size();

        OAuth2User oauthUser = mockOAuthUser(email, sub, name);
        OAuth2UserRequest request = mock(OAuth2UserRequest.class);

        when(userRepository.findByEmailWithAuths(email)).thenReturn(user);

        CustomOAuth2UserService spyService = spy(customOAuth2UserService);
        doReturn(oauthUser).when(spyService).getOAuthUser(any());

        // when
        spyService.loadUser(request);

        // then
        assertThat(user.getUserAuths().size()).isEqualTo(beforeSize);
    }
}