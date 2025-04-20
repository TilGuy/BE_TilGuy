package com.tilguys.matilda.common.auth.service;

import com.tilguys.matilda.common.auth.exception.NotExistUserException;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void validateExistUser(String identifier) {
        userRepository.findByIdentifier(identifier)
                .orElseThrow(NotExistUserException::new);
    }

    public Optional<TilUser> findUserByIdentifier(String userIdentifier) {
        return userRepository.findByIdentifier(userIdentifier);
    }

    public void signup(String identifier) {
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
}
