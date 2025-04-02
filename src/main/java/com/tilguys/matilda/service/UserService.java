package com.tilguys.matilda.service;

import com.tilguys.matilda.config.jwt.JwtTokenFactory;
import com.tilguys.matilda.exception.MatildaException;
import com.tilguys.matilda.user.entity.ProviderInfo;
import com.tilguys.matilda.user.entity.Role;
import com.tilguys.matilda.user.entity.User;
import com.tilguys.matilda.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String USER_DOESNT_EXIST = "유저를 찾을 수 없습니다.";

    private final UserRepository userRepository;
    private final JwtTokenFactory jwtTokenFactory;

    public void validateExistUser(String identifier) {
        userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new MatildaException(USER_DOESNT_EXIST));
    }

    public Optional<User> findUserByIdentifier(String userIdentifier) {
        return userRepository.findByIdentifier(userIdentifier);
    }

    public void signup(String identifier) {
        User user = User.builder()
                .identifier(identifier)
                .providerInfo(ProviderInfo.GITHUB)
                .role(Role.USER)
                .build();
        userRepository.save(user);
    }
}
