package com.tilguys.matilda.common.auth.service;

import com.tilguys.matilda.common.auth.GithubUserInfo;
import com.tilguys.matilda.common.auth.exception.DoesNotExistUserException;
import com.tilguys.matilda.common.auth.exception.NotExistUserException;
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

    private final UserRepository userRepository;

    public void validateExistUser(String identifier) {
        if (!userRepository.existsByIdentifier(identifier)) {
            throw new NotExistUserException();
        }
    }

    public Optional<TilUser> findUserByIdentifier(String userIdentifier) {
        return userRepository.findByIdentifier(userIdentifier);
    }

    public void signup(String identifier, String nickname) {
        if (nickname == null) {
            nickname = identifier;
        }
        if (userRepository.existsByIdentifier(identifier)) {
            return;
        }
        TilUser tilUser = TilUser.builder()
                .identifier(identifier)
                .nickname(nickname)
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

    public TilUser getById(Long memberId) {
        return findById(memberId).orElseThrow(NotExistUserException::new);
    }
}
