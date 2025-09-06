    package com.tilguys.matilda.til.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TilCreationLockService {

    private final TilCreationLockRepository lockRepository;

    /**
     * TIL 생성을 위한 분산락을 획득합니다.
     * 
     * @param userId 사용자 ID
     * @param lockDate 락을 걸 날짜
     * @return 락 획득 성공 여부
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean acquireLock(Long userId, LocalDate lockDate) {
        try {
            cleanupExpiredLocks();
            
            Optional<TilCreationLock> existingLock = lockRepository.findByUserIdAndLockDate(userId, lockDate);
            
            if (existingLock.isPresent()) {
                TilCreationLock lock = existingLock.get();
                if (lock.isExpired()) {
                    lockRepository.delete(lock);
                    return createNewLock(userId, lockDate);
                } else {
                    log.warn("이미 존재하는 락 - 사용자ID: {}, 날짜: {}", userId, lockDate);
                    return false;
                }
            }
            
            return createNewLock(userId, lockDate);
            
        } catch (DataIntegrityViolationException e) {
            log.warn("동시 접근으로 인한 락 획득 실패 - 사용자ID: {}, 날짜: {}", userId, lockDate);
            return false;
        } catch (Exception e) {
            log.error("예상치 못한 오류로 인한 락 획득 실패 - 사용자ID: {}, 날짜: {}", userId, lockDate, e);
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void releaseLock(Long userId, LocalDate lockDate) {
        try {
            lockRepository.deleteByUserIdAndLockDate(userId, lockDate);
            log.debug("락 해제 완료 - 사용자ID: {}, 날짜: {}", userId, lockDate);
        } catch (Exception e) {
            log.error("락 해제 실패 - 사용자ID: {}, 날짜: {}", userId, lockDate, e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanupExpiredLocks() {
        try {
            lockRepository.deleteExpiredLocks(LocalDateTime.now());
        } catch (Exception e) {
            log.error("만료된 락 정리 실패", e);
        }
    }

    private boolean createNewLock(Long userId, LocalDate lockDate) {
        try {
            TilCreationLock newLock = TilCreationLock.create(userId, lockDate);
            lockRepository.save(newLock);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}
