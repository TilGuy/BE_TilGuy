package com.tilguys.matilda.til.service;

import com.tilguys.matilda.common.auth.Jwt;
import com.tilguys.matilda.common.auth.UserRefreshToken;
import com.tilguys.matilda.common.auth.repository.UserRefreshTokenRepository;
import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.user.TilUser;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        LocalDateTime newExpireDate = LocalDateTime.now().plusSeconds(refreshTokenAliveSecond);
        UserRefreshToken refreshToken = createAllUpdateRefreshToken(tilUser, newExpireDate);
        userRefreshTokenRepository.save(refreshToken);
    }

    private UserRefreshToken createAllUpdateRefreshToken(TilUser tilUser, LocalDateTime newExpireDate) {
        return userRefreshTokenRepository.findByUserId(tilUser.getId())
                .map(existingToken -> {
                    // 기존 토큰이 있으면 만료 시간 업데이트
                    existingToken.setExpireDate(newExpireDate);
                    return existingToken;
                })
                .orElseGet(() ->
                        // 기존 토큰이 없으면 새로 생성
                        UserRefreshToken.builder()
                                .userId(tilUser.getId())
                                .expireDate(newExpireDate)
                                .build()
                );
    }

    @Transactional
    public void deleteRefreshTokenByUserId(Long userId) {
        userRefreshTokenRepository.deleteByUserId(userId);
    }
}
