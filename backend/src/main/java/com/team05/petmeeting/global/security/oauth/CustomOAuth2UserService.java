package com.team05.petmeeting.global.security.oauth;

import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.entity.UserAuth;
import com.team05.petmeeting.domain.user.provider.Provider;
import com.team05.petmeeting.domain.user.repository.UserRepository;
import com.team05.petmeeting.global.security.userdetails.CustomOAuth2User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User googleUser = getOAuthUser(userRequest);

        log.info("user: {}", googleUser.getAttributes());

        String email = googleUser.getAttribute("email");
        String providerId = googleUser.getAttribute("sub");
        String name = googleUser.getAttribute("name");

        Optional<User> userOptional = userRepository.findByEmailWithAuths(email); // 해당 이메일의 사용자 존재 여부 검사
        User user;

        if (userOptional.isPresent()) { // 해당 이메일의 회원 존재
            user = userOptional.get();

            if (!hasGoogleAuth(user)) { // 구글 로그인한 적 없는 회원 only LOCAL -> 계정 자동 통합
                UserAuth googleAuth = UserAuth.create(Provider.GOOGLE, providerId, null);
                user.addAuth(googleAuth);
            }
            // 구글 로그인 존재 회원 -> 그대로 가입 진행
        } else { // 해당 이메일의 회원이 존재하지않음 -> 사용자 및 구글 로그인 정보 새로 생성
            user = User.create(email, generateNickname(name), name);
            UserAuth googleAuth = UserAuth.create(Provider.GOOGLE, providerId, null);
            user.addAuth(googleAuth);
        }
        userRepository.save(user);

        return new CustomOAuth2User(user);
    }

    private String generateNickname(String name) {
        int random = (int) (Math.random() * 10000);
        return name + "_" + random;
    }

    private boolean hasGoogleAuth(User user) {
        return user.getUserAuths().stream()
                .anyMatch(auth -> auth.getProvider().equals(Provider.GOOGLE));
    }

    protected OAuth2User getOAuthUser(OAuth2UserRequest request) {
        return super.loadUser(request);
    }
}
