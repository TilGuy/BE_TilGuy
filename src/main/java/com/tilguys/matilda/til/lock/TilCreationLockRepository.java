package com.tilguys.matilda.til.lock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TilCreationLockRepository extends JpaRepository<TilCreationLock, Long> {

    Optional<TilCreationLock> findByUserIdAndLockDate(Long userId, LocalDate lockDate);

    @Modifying
    @Query("DELETE FROM TilCreationLock l WHERE l.expiresAt < :now")
    void deleteExpiredLocks(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM TilCreationLock l WHERE l.userId = :userId AND l.lockDate = :lockDate")
    void deleteByUserIdAndLockDate(@Param("userId") Long userId, @Param("lockDate") LocalDate lockDate);
}
