package com.tilguys.matilda.til.service;

import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.UserRefreshToken;
import com.tilguys.matilda.common.auth.repository.UserRefreshTokenRepository;
import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.user.TilUser;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRefreshTokenService {

    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final UserService userService;

    public boolean isReLoginPossible(Long id) {
        return userRefreshTokenRepository.existsByUserIdAndExpireDateAfter(id, LocalDateTime.now());
    }

    public void addRefreshToken(String identifier) {
        userService.validateExistUser(identifier);
        TilUser tilUser = userService.findUserByIdentifier(identifier).get();
        long refreshTokenAliveSecond = Jwt.getRefreshTokenAliveSecond();
        UserRefreshToken refreshToken = UserRefreshToken.builder()
                .userId(tilUser.getId())
                .expireDate(LocalDateTime.now().plusSeconds(refreshTokenAliveSecond))
                .build();
        userRefreshTokenRepository.save(refreshToken);
    }
}
