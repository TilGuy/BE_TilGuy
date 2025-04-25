package com.tilguys.matilda.common.auth.service;

import com.tilguys.matilda.common.auth.GithubUserInfo;
import com.tilguys.matilda.common.auth.exception.DoesNotExistUserException;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String USER_DOESNT_EXIST = "유저를 찾을 수 없습니다.";

    private final UserRepository userRepository;

    public Optional<TilUser> findUserByIdentifier(String userIdentifier) {
        return userRepository.findByIdentifier(userIdentifier);
    }

    public void signup(String identifier) {
        if (userRepository.existsByIdentifier(identifier)) {
            return;
        }
        TilUser tilUser = TilUser.builder()
                .identifier(identifier)
                .providerInfo(ProviderInfo.GITHUB)
                .role(Role.USER)
                .build();
        userRepository.save(tilUser);
    }

    public Optional<TilUser> findById(long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void updateUserInfo(GithubUserInfo githubUserInfo) {
        TilUser userByIdentifier = findUserByIdentifier(githubUserInfo.identifier())
                .orElseThrow(DoesNotExistUserException::new);
        userByIdentifier.updateAvatarUrl(githubUserInfo.avatarUrl());
        userByIdentifier.updateNickname(githubUserInfo.nickname());
    }
}
