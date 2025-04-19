package com.tilguys.matilda.til.service;

import com.tilguys.matilda.til.repository.UserRefreshTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRefreshTokenService {

    private final UserRefreshTokenRepository userRefreshTokenRepository;

    public boolean isReLoginPossible(Long id) {
        return userRefreshTokenRepository.existsByUserIdAndExpireDateAfter(id, LocalDateTime.now());
    }
}
