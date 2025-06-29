package com.tilguys.matilda.tag.event;

import com.tilguys.matilda.tag.service.TilTagService;
import com.tilguys.matilda.til.event.TilCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class TilTagGenerateListener {

    private final TilTagService tilTagService;

    public TilTagGenerateListener(TilTagService tilTagService) {
        this.tilTagService = tilTagService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("tilTagTaskExecutor")
    public void handleTilReferenceExtractionRequested(TilCreatedEvent tilCreatedEvent) {
        log.debug("TIL 태그 추출 ");
        tilTagService.createTags(tilCreatedEvent);
    }
}
