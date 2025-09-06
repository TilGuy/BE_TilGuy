package com.tilguys.matilda.til.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TilCreationLockServiceTest {

    @Autowired
    private TilCreationLockService lockService;

    @Autowired
    private TilCreationLockRepository lockRepository;

    private final Long userId = 1L;
    private final LocalDate testDate = LocalDate.of(2024, 1, 1);

    @BeforeEach
    void setUp() {
        lockRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("락 획득 성공 테스트")
    void acquireLock_Success() {
        // when
        boolean acquired = lockService.acquireLock(userId, testDate);

        // then
        assertThat(acquired).isTrue();
        assertThat(lockRepository.findByUserIdAndLockDate(userId, testDate)).isPresent();
    }

    @Test
    @Transactional
    @DisplayName("동일한 락 중복 획득 시도 시 실패")
    void acquireLock_DuplicateAttempt_ShouldFail() {
        // given
        lockService.acquireLock(userId, testDate);

        // when
        boolean secondAttempt = lockService.acquireLock(userId, testDate);

        // then
        assertThat(secondAttempt).isFalse();
    }

    @Test
    @Transactional
    @DisplayName("서로 다른 사용자의 같은 날짜 락 획득은 성공")
    void acquireLock_DifferentUsers_ShouldSucceed() {
        // given
        Long user1 = 1L;
        Long user2 = 2L;

        // when
        boolean user1Acquired = lockService.acquireLock(user1, testDate);
        boolean user2Acquired = lockService.acquireLock(user2, testDate);

        // then
        assertThat(user1Acquired).isTrue();
        assertThat(user2Acquired).isTrue();
        assertThat(lockRepository.findByUserIdAndLockDate(user1, testDate)).isPresent();
        assertThat(lockRepository.findByUserIdAndLockDate(user2, testDate)).isPresent();
    }

    @Test
    @Transactional
    @DisplayName("같은 사용자의 서로 다른 날짜 락 획득은 성공")
    void acquireLock_SameUserDifferentDates_ShouldSucceed() {
        // given
        LocalDate date1 = LocalDate.of(2024, 1, 1);
        LocalDate date2 = LocalDate.of(2024, 1, 2);

        // when
        boolean date1Acquired = lockService.acquireLock(userId, date1);
        boolean date2Acquired = lockService.acquireLock(userId, date2);

        // then
        assertThat(date1Acquired).isTrue();
        assertThat(date2Acquired).isTrue();
        assertThat(lockRepository.findByUserIdAndLockDate(userId, date1)).isPresent();
        assertThat(lockRepository.findByUserIdAndLockDate(userId, date2)).isPresent();
    }

    @Test
    @Transactional
    @DisplayName("락 해제 테스트")
    void releaseLock_Success() {
        // given
        lockService.acquireLock(userId, testDate);
        assertThat(lockRepository.findByUserIdAndLockDate(userId, testDate)).isPresent();

        // when
        lockService.releaseLock(userId, testDate);

        // then
        assertThat(lockRepository.findByUserIdAndLockDate(userId, testDate)).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("만료된 락 정리 테스트")
    void cleanupExpiredLocks_Success() {
        // given - 만료된 락 직접 생성
        TilCreationLock expiredLock = TilCreationLock.builder()
                .userId(userId)
                .lockDate(testDate)
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .expiresAt(LocalDateTime.now().minusMinutes(5)) // 5분 전에 만료
                .build();
        lockRepository.save(expiredLock);

        TilCreationLock validLock = TilCreationLock.builder()
                .userId(2L)
                .lockDate(testDate.plusDays(1))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5)) // 유효한 락
                .build();
        lockRepository.save(validLock);

        // when
        lockService.cleanupExpiredLocks();

        // then
        assertThat(lockRepository.findByUserIdAndLockDate(userId, testDate)).isEmpty();
        assertThat(lockRepository.findByUserIdAndLockDate(2L, testDate.plusDays(1))).isPresent();
    }

    @Test
    @Transactional
    @DisplayName("만료된 락 재획득 테스트")
    void acquireLock_ExpiredLock_ShouldReacquire() {
        // given - 만료된 락 생성
        TilCreationLock expiredLock = TilCreationLock.builder()
                .userId(userId)
                .lockDate(testDate)
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .expiresAt(LocalDateTime.now().minusMinutes(1)) // 1분 전에 만료
                .build();
        lockRepository.save(expiredLock);

        // when
        boolean acquired = lockService.acquireLock(userId, testDate);

        // then
        assertThat(acquired).isTrue();
        
        TilCreationLock newLock = lockRepository.findByUserIdAndLockDate(userId, testDate).orElseThrow();
        assertThat(newLock.getId()).isNotEqualTo(expiredLock.getId());
        assertThat(newLock.isExpired()).isFalse();
    }

    @Test
    @DisplayName("동시성 테스트 - 10개 스레드가 동시에 락 획득 시도")
    void concurrentLockAcquisition_OnlyOneShouldSucceed() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    boolean acquired = lockService.acquireLock(userId, testDate);
                    if (acquired) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            }, executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(30, TimeUnit.SECONDS);
        assertThat(terminated).isTrue();

        // then
        assertThat(successCount.get()).isEqualTo(1); // 정확히 하나만 성공
        assertThat(failureCount.get()).isEqualTo(threadCount - 1); // 나머지는 모두 실패
        assertThat(lockRepository.findByUserIdAndLockDate(userId, testDate)).isPresent();
    }

    @Test
    @DisplayName("서로 다른 키에 대한 동시성 테스트 - 모든 스레드가 성공해야 함")
    void concurrentLockAcquisition_DifferentKeys_AllShouldSucceed() throws InterruptedException {
        // given
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when 
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int dayOffset = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                LocalDate uniqueDate = testDate.plusDays(dayOffset);
                boolean acquired = lockService.acquireLock(userId, uniqueDate);
                if (acquired) {
                    successCount.incrementAndGet();
                }
            }, executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // then
        assertThat(successCount.get()).isEqualTo(threadCount); // 모든 스레드가 성공
        
        for (int i = 0; i < threadCount; i++) {
            LocalDate uniqueDate = testDate.plusDays(i);
            assertThat(lockRepository.findByUserIdAndLockDate(userId, uniqueDate)).isPresent();
        }
    }

    @Test
    @DisplayName("락 획득 후 해제 동시성 테스트")
    void concurrentAcquireAndRelease_ShouldBeConsistent() throws InterruptedException {
        // given
        int iterations = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        // when
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            final int index = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    LocalDate uniqueDate = testDate.plusDays(index);
                    if (lockService.acquireLock(userId, uniqueDate)) {
                        // 락 획득 성공 시 잠시 후 해제
                        Thread.sleep(10);
                        lockService.releaseLock(userId, uniqueDate);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(30, TimeUnit.SECONDS);
        assertThat(terminated).isTrue();

        // then - 모든 작업 완료 후 락들이 해제되어 있어야 함
        for (int i = 0; i < iterations; i++) {
            LocalDate uniqueDate = testDate.plusDays(i);
            assertThat(lockRepository.findByUserIdAndLockDate(userId, uniqueDate)).isEmpty();
        }
    }
}
