package com.tilguys.matilda.common.auth.repository;

import com.tilguys.matilda.common.auth.UserRefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {

    boolean existsByUserId(Long userId);

    boolean existsByUserIdAndExpireDateAfter(Long userId, LocalDateTime dateTime);

    Optional<UserRefreshToken> findByUserId(Long id);

    void deleteByUserId(Long userId);
}
