package com.tilguys.matilda.reference.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilguys.matilda.common.external.OpenAIClient;
import com.tilguys.matilda.reference.domain.TilReferenceGenerator;
import com.tilguys.matilda.reference.domain.TilReferenceParser;
import com.tilguys.matilda.reference.repository.ReferenceRepository;
import com.tilguys.matilda.reference.service.event.ReferenceExtractionCompletedEvent;
import com.tilguys.matilda.reference.service.event.ReferenceExtractionRequestedEvent;
import com.tilguys.matilda.til.domain.Reference;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.repository.TilRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
public class AsyncReferenceService {

    private final OpenAIClient openAIClient;
    private final TilReferenceGenerator tilReferenceGenerator;
    private final TilReferenceParser tilReferenceParser;
    private final ReferenceRepository referenceRepository;
    private final TilRepository tilRepository;
    private final ApplicationEventPublisher eventPublisher;


    public AsyncReferenceService(OpenAIClient openAIClient,
                                 ObjectMapper objectMapper,
                                 ReferenceRepository referenceRepository,
                                 ApplicationEventPublisher eventPublisher,
                                 TilRepository tilRepository
    ) {
        this.tilReferenceGenerator = new TilReferenceGenerator();
        this.tilReferenceParser = new TilReferenceParser(objectMapper);
        this.openAIClient = openAIClient;
        this.referenceRepository = referenceRepository;
        this.eventPublisher = eventPublisher;
        this.tilRepository = tilRepository;
    }

    public List<Reference> extractTilReference(String tilContent) {
        String responseJson = openAIClient.callOpenAI(
                tilReferenceGenerator.createPrompt(tilContent),
                tilReferenceGenerator.createFunctionDefinition()
        );
        return tilReferenceParser.parseReferences(responseJson);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("tilReferenceTaskExecutor")
    public void handleTilReferenceExtractionRequested(ReferenceExtractionRequestedEvent event) {
        try {
            List<Reference> references = extractTilReference(event.tilContent());
            eventPublisher.publishEvent(
                    new ReferenceExtractionCompletedEvent(event.tilId(), references)
            );
        } catch (Exception e) {
            log.error("TIL Reference 추출 실패: tilId={}, {}", event.tilId(), e.getMessage());
        }
    }

    @EventListener
    @Transactional
    public void handleTilReferenceExtractionCompleted(ReferenceExtractionCompletedEvent event) {
        try {
            Til til = tilRepository.findById(event.tilId())
                    .orElseThrow(() -> new IllegalArgumentException("TIL을 찾을 수 없습니다"));

            List<Reference> references = event.references();
            references.forEach(reference -> reference.setTil(til));

            referenceRepository.saveAll(references);

            log.debug("TIL Reference 저장 완료: tilId={}", event.tilId());
        } catch (Exception e) {
            log.error("TIL Reference 저장 실패: tilId={}", event.tilId(), e);
        }
    }
}
