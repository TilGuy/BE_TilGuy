package com.tilguys.matilda.reference.event;

import com.tilguys.matilda.reference.service.ReferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TilReferenceGeneratorListener {

    private final ReferenceService referenceService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("tilReferenceTaskExecutor")
    public void handleTilReferenceCreateEvent(ReferenceCreateEvent event) {
        try {
            log.debug("TIL Reference 추출 ");
            referenceService.createReference(event);
        } catch (Exception e) {
            log.error("TIL Reference 추출 실패: tilId={}, {}", event.tilId(), e.getMessage());
        }
    }
}
