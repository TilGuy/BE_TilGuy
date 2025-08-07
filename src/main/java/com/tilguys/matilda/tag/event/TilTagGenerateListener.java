package com.tilguys.matilda.tag.event;

import com.tilguys.matilda.tag.service.TilTagService;
import com.tilguys.matilda.til.event.TilCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TilTagGenerateListener {

    private static final Logger log = LoggerFactory.getLogger(TilTagGenerateListener.class);
    private final TilTagService tilTagService;

    public TilTagGenerateListener(TilTagService tilTagService) {
        this.tilTagService = tilTagService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("tilTagTaskExecutor")
    public void handleTilReferenceExtractionRequested(TilCreatedEvent tilCreatedEvent) {
        log.debug("TIL 태그 추출 시작");
        
        try {
            // 먼저 직접 Failover AI 호출 시도
            tilTagService.createTagsDirect(tilCreatedEvent);
            log.info("Tag creation succeeded immediately for TIL {}", tilCreatedEvent.getTilId());
            
        } catch (Exception e) {
            log.warn("Direct tag creation failed for TIL {}, scheduling via Outbox: {}", 
                    tilCreatedEvent.getTilId(), e.getMessage());
            
            // 실패 시에만 Outbox에 저장해서 나중에 재시도
            tilTagService.createTags(tilCreatedEvent);
        }
    }
}
