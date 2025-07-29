package com.tilguys.matilda.tag.service;

import com.tilguys.matilda.tag.domain.OutboxEventStatus;
import com.tilguys.matilda.tag.domain.TagCreationOutboxEvent;
import com.tilguys.matilda.tag.repository.TagCreationOutboxEventRepository;
import com.tilguys.matilda.til.event.TilCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TagCreationOutboxService {

    private static final Logger log = LoggerFactory.getLogger(TagCreationOutboxService.class);

    private final TagCreationOutboxEventRepository outboxRepository;
    private final TilTagService tilTagService;

    public TagCreationOutboxService(
            TagCreationOutboxEventRepository outboxRepository,
            @Lazy TilTagService tilTagService
    ) {
        this.outboxRepository = outboxRepository;
        this.tilTagService = tilTagService;
    }

    /**
     * 태그 생성 이벤트를 Outbox에 저장 (트랜잭션 안전)
     */
    @Transactional
    public void scheduleTagCreation(TilCreatedEvent tilCreatedEvent) {
        TagCreationOutboxEvent outboxEvent = TagCreationOutboxEvent.builder()
                .tilId(tilCreatedEvent.getTilId())
                .tilContent(tilCreatedEvent.getTilContent())
                .userId(tilCreatedEvent.getUserId())
                .status(OutboxEventStatus.PENDING)
                .scheduledAt(LocalDateTime.now())
                .build();

        outboxRepository.save(outboxEvent);

        log.info("Tag creation scheduled for TIL {}", tilCreatedEvent.getTilId());

        // 즉시 비동기 처리 시도
        processEventAsync(outboxEvent.getId());
    }

    /**
     * 비동기로 이벤트 처리
     */
    @Async
    public void processEventAsync(Long eventId) {
        try {
            // 약간의 지연 후 처리 (동시성 이슈 방지)
            Thread.sleep(100);
            processEvent(eventId);
        } catch (Exception e) {
            log.error("Async tag creation processing failed for event {}", eventId, e);
        }
    }

    /**
     * 개별 이벤트 처리
     */
    @Transactional
    public void processEvent(Long eventId) {
        TagCreationOutboxEvent event = outboxRepository.findById(eventId)
                .orElse(null);

        if (event == null) {
            log.warn("Outbox event {} not found", eventId);
            return;
        }

        if (event.getStatus() != OutboxEventStatus.PENDING) {
            log.warn("Event {} is not pending (status: {}), skipping", eventId, event.getStatus());
            return;
        }

        try {
            log.info("Processing tag creation for TIL {} (event: {})", event.getTilId(), eventId);

            // 처리 중 상태로 변경
            event.markAsProcessing();
            outboxRepository.save(event);

            // 실제 태그 생성 로직 호출
            TilCreatedEvent tilCreatedEvent = new TilCreatedEvent(
                    event.getTilId(),
                    event.getTilContent(),
                    event.getUserId()
            );

            tilTagService.createTagsDirect(tilCreatedEvent);

            // 성공 시 완료 처리
            event.markAsCompleted();
            outboxRepository.save(event);

            log.info("Tag creation completed for TIL {} (event: {})", event.getTilId(), eventId);

        } catch (Exception e) {
            log.error(
                    "Tag creation failed for TIL {} (event: {}): {}",
                    event.getTilId(), eventId, e.getMessage(), e
            );

            // 실패 처리
            event.incrementRetryCount();
            event.markAsFailed(e.getMessage());

            // 재시도 가능하면 스케줄링
            if (event.canRetry()) {
                LocalDateTime nextRetry = LocalDateTime.now()
                        .plusMinutes(5L * event.getRetryCount());
                event.reschedule(nextRetry);
                log.info(
                        "Tag creation rescheduled for TIL {} at {} (retry: {})",
                        event.getTilId(), nextRetry, event.getRetryCount()
                );
            }

            outboxRepository.save(event);
        }
    }

    /**
     * 주기적으로 대기 중인 이벤트들 처리
     */
    @Scheduled(fixedDelay = 30000) // 30초마다
    @Transactional
    public void processPendingEvents() {
        List<TagCreationOutboxEvent> pendingEvents =
                outboxRepository.findPendingEvents(LocalDateTime.now());

        if (!pendingEvents.isEmpty()) {
            log.info("Processing {} pending tag creation events", pendingEvents.size());

            for (TagCreationOutboxEvent event : pendingEvents) {
                try {
                    processEvent(event.getId());
                } catch (Exception e) {
                    log.error("Failed to process pending event {}", event.getId(), e);
                }
            }
        }
    }

    /**
     * 실패한 이벤트들 재시도
     */
    @Scheduled(fixedDelay = 60000) // 1분마다
    @Transactional
    public void retryFailedEvents() {
        List<TagCreationOutboxEvent> retryableEvents =
                outboxRepository.findRetryableFailedEvents(LocalDateTime.now());

        if (!retryableEvents.isEmpty()) {
            log.info("Retrying {} failed tag creation events", retryableEvents.size());

            for (TagCreationOutboxEvent event : retryableEvents) {
                try {
                    processEvent(event.getId());
                } catch (Exception e) {
                    log.error("Failed to retry event {}", event.getId(), e);
                }
            }
        }
    }

    /**
     * 오래된 이벤트 정리
     */
    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime cutoffDate = LocalDateTime.now()
                .minusDays(7);
        List<TagCreationOutboxEvent> oldEvents =
                outboxRepository.findOldProcessedEvents(cutoffDate);

        if (!oldEvents.isEmpty()) {
            log.info("Cleaning up {} old outbox events", oldEvents.size());
            outboxRepository.deleteAll(oldEvents);
        }
    }

    /**
     * 특정 TIL의 태그 생성 상태 확인
     */
    public boolean isTagCreationCompleted(Long tilId) {
        List<TagCreationOutboxEvent> completedEvents =
                outboxRepository.findByTilIdAndStatus(tilId, OutboxEventStatus.COMPLETED);
        return !completedEvents.isEmpty();
    }

    /**
     * 이벤트 통계 조회
     */
    public void logEventStatistics() {
        LocalDateTime fromDate = LocalDateTime.now()
                .minusDays(1);
        List<Object[]> stats = outboxRepository.getEventStatistics(fromDate);

        log.info("=== Tag Creation Outbox Statistics (24h) ===");
        for (Object[] stat : stats) {
            log.info("Status: {}, Count: {}", stat[0], stat[1]);
        }
    }
}
