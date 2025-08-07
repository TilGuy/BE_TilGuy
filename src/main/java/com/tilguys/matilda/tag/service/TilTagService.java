package com.tilguys.matilda.tag.service;

import com.tilguys.matilda.common.external.FailoverAIServiceManager;
import com.tilguys.matilda.tag.domain.SubTag;
import com.tilguys.matilda.tag.domain.TilTagGenerator;
import com.tilguys.matilda.tag.domain.TilTagParser;
import com.tilguys.matilda.tag.domain.TilTags;
import com.tilguys.matilda.tag.repository.SubTagRepository;
import com.tilguys.matilda.tag.repository.TagRepository;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.event.TilCreatedEvent;
import com.tilguys.matilda.til.service.TilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class TilTagService {

    private static final Logger log = LoggerFactory.getLogger(TilTagService.class);

    private final FailoverAIServiceManager failoverAIServiceManager;
    private final TilTagGenerator tagGenerator;
    private final TilTagParser tagParser;
    private final TagRepository tagRepository;
    private final SubTagRepository subTagRepository;
    private final TilService tilService;
    private final TagCreationOutboxService tagCreationOutboxService;

    public TilTagService(
            @Autowired FailoverAIServiceManager failoverAIServiceManager,
            TagRepository tagRepository,
            SubTagRepository subTagRepository,
            TilService tilService,
            TagCreationOutboxService tagCreationOutboxService
    ) {
        this.tagRepository = tagRepository;
        this.subTagRepository = subTagRepository;
        this.tagGenerator = new TilTagGenerator();
        this.tagParser = new TilTagParser();
        this.failoverAIServiceManager = failoverAIServiceManager;
        this.tilService = tilService;
        this.tagCreationOutboxService = tagCreationOutboxService;
    }

    public String requestTilTagResponseJson(String tilContent) {
        return failoverAIServiceManager.callAIWithSimpleFallback(
                tagGenerator.createPrompt(tilContent),
                tagGenerator.createFunctionDefinition()
        );
    }

    /**
     * Transaction Outbox 패턴을 사용한 태그 생성 (트랜잭션 안전)
     */
    @Transactional
    public void createTags(TilCreatedEvent tilCreatedEvent) {
        // Outbox에 이벤트 저장 후 비동기 처리
        tagCreationOutboxService.scheduleTagCreation(tilCreatedEvent);
    }

    /**
     * 직접 태그 생성 (Outbox 서비스에서 호출)
     */
    @Transactional
    public void createTagsDirect(TilCreatedEvent tilCreatedEvent) {
        try {
            Til til = tilService.getTilByTilId(tilCreatedEvent.getTilId());
            String tilResponseJson = requestTilTagResponseJson(tilCreatedEvent.getTilContent());

            List<Tag> tags = saveTilTags(tilResponseJson)
                    .stream()
                    .toList();

            til.updateTags(tags);

            TilTags tilTags = new TilTags(tags);
            createSubTags(tilResponseJson, tilTags);
        } catch (Exception e) {
            throw new RuntimeException("태그 생성 실패:" + e.getMessage());
        }
    }

    /**
     * Retry 로직이 포함된 태그 생성 (테스트용)
     * maxAttempts = 2, delay = 1000ms
     */
    @Transactional
    public void createTagsWithRetry(TilCreatedEvent tilCreatedEvent) {
        int maxAttempts = 2;
        int delayMs = 1000;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("Tag creation attempt {} for TIL {}", attempt, tilCreatedEvent.getTilId());
                
                Til til = tilService.getTilByTilId(tilCreatedEvent.getTilId());
                String tilResponseJson = requestTilTagResponseJson(tilCreatedEvent.getTilContent());

                List<Tag> tags = saveTilTags(tilResponseJson)
                        .stream()
                        .toList();

                til.updateTags(tags);

                TilTags tilTags = new TilTags(tags);
                createSubTags(tilResponseJson, tilTags);
                
                log.info("Tag creation succeeded on attempt {} for TIL {}", attempt, tilCreatedEvent.getTilId());
                return; // 성공 시 즉시 리턴

            } catch (Exception e) {
                lastException = e;
                log.warn("Tag creation attempt {} failed for TIL {}: {}", 
                        attempt, tilCreatedEvent.getTilId(), e.getMessage());

                if (attempt < maxAttempts) {
                    try {
                        log.info("Waiting {}ms before retry...", delayMs);
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry delay", ie);
                    }
                }
            }
        }

        // 모든 재시도 실패
        throw new RuntimeException("태그 생성 실패 (모든 재시도 소진): " + lastException.getMessage(), lastException);
    }

    @Transactional
    public List<Tag> saveTilTags(String responseJson) {
        List<Tag> tags = extractTilTags(responseJson);
        return tagRepository.saveAll(tags);
    }

    private List<Tag> extractTilTags(String responseJson) {
        Set<String> tags = tagParser.parseTags(responseJson);
        return tags.stream()
                .map(Tag::new)
                .toList()
                .subList(0, Math.min(5, tags.size()));
    }

    private List<SubTag> extractSubTilTags(String responseJson, TilTags coreTags) {
        List<SubTag> subTags = tagParser.parseSubTags(responseJson, coreTags);
        return subTags.subList(0, Math.min(25, subTags.size()));
    }

    @Transactional
    public List<SubTag> createSubTags(String tilResponseJson, TilTags tilTags) {
        List<SubTag> subTags = extractSubTilTags(tilResponseJson, tilTags);
        return subTagRepository.saveAll(subTags);
    }

    public List<Tag> getRecentWroteTags(LocalDate recent) {
        return tagRepository.findByCreatedAtGreaterThanEqual(recent.atStartOfDay());
    }

    public List<SubTag> getRecentSubTags(LocalDate recent) {
        return subTagRepository.findByCreatedAtGreaterThanEqual(recent.atStartOfDay());
    }
}
