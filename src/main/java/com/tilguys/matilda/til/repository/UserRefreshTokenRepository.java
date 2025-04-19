package com.tilguys.matilda.til.repository;

import com.tilguys.matilda.auth.user.UserRefreshToken;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {

    boolean existsByUserId(Long userId);

    boolean existsByUserIdAndExpireDateAfter(Long userId, LocalDateTime dateTime);
}
