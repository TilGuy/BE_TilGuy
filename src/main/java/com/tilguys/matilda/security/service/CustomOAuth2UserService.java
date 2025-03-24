package com.tilguys.matilda.security.service;

import com.tilguys.matilda.security.info.OAuth2UserInfo;
import com.tilguys.matilda.security.info.OAuth2UserInfoFactory;
import com.tilguys.matilda.user.entity.ProviderInfo;
import com.tilguys.matilda.user.entity.Role;
import com.tilguys.matilda.user.entity.User;
import com.tilguys.matilda.user.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // OAuth2 로그인 진행 시 키가 되는 필드값. Primary Key와 같은 의미.
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName();

        String providerCode = userRequest.getClientRegistration().getRegistrationId();
        ProviderInfo providerInfo = ProviderInfo.from(providerCode);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerInfo, attributes);
        String userIdentifier = oAuth2UserInfo.getUserIdentifier();

        // 기존 유저 -> 그대로 가져옴
        // 신규 유저 -> NOT_REGISTERED 로 생성
        User user = getUser(userIdentifier, providerInfo);

        // Security Context에 저장할 객체 생성
        return new UserPrincipal(user, attributes, userNameAttributeName);
    }

    private User getUser(String userIdentifier, ProviderInfo providerInfo) {
        Optional<User> optionalUser = userRepository.findByOAuthInfo(userIdentifier, providerInfo);

        if (optionalUser.isEmpty()) {
            User unregisteredUser = User.builder()
                    .identifier(userIdentifier)
                    .role(Role.USER)
                    .providerInfo(providerInfo)
                    .build();
            return userRepository.save(unregisteredUser);
        }
        return optionalUser.get();
    }
}
