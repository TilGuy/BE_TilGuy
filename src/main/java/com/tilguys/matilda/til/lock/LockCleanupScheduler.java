package com.tilguys.matilda.til.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockCleanupScheduler {

    private final TilCreationLockService lockService;

 
    @Scheduled(fixedRate = 600000) // 10분
    public void cleanupExpiredLocks() {
        lockService.cleanupExpiredLocks();
    }
}
