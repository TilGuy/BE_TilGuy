package com.tilguys.matilda.tag.event;

import com.tilguys.matilda.reference.service.TilReferenceService;
import com.tilguys.matilda.tag.domain.TilTags;
import com.tilguys.matilda.tag.service.TilTagService;
import com.tilguys.matilda.til.domain.Reference;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.event.TilCreatedEvent;
import com.tilguys.matilda.til.service.TilService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TilTagGenerateListener {

    private final TilService tilService;
    private final TilTagService tilTagService;
    private final TilReferenceService tilReferenceService;

    public TilTagGenerateListener(TilService tilService, TilTagService tilTagService,
                                  TilReferenceService tilReferenceService) {
        this.tilService = tilService;
        this.tilTagService = tilTagService;
        this.tilReferenceService = tilReferenceService;
    }

    @Async
    @EventListener
    public void handleTilCreated(TilCreatedEvent tilCreatedEvent) {
        try {
            Til til = tilService.getTilByTilId(tilCreatedEvent.getTilId());
            String tilResponseJson = tilTagService.requestTilTagResponseJson(til.getContent());

            List<Tag> tags = tilTagService.saveTilTags(tilResponseJson)
                    .stream()
                    .toList();

            String tagResults = tags.stream()
                    .map(Tag::getTagString)
                    .collect(Collectors.joining(","));
            log.debug("{}=> {} => 추출된 태그 =>{}", til.getContent(), tilResponseJson, tagResults);

            til.updateTags(tags);
            List<Reference> references = tilReferenceService.extractTilReference(til.getContent());
            til.updateReferences(references);

            TilTags tilTags = new TilTags(tags);
            tilTagService.createSubTags(tilResponseJson, tilTags);
        } catch (Exception e) {
            log.info("태그 생성 실패 - TIL ID: {}, Error: {}", tilCreatedEvent.getTilId(), e.getMessage());
        }
    }
}
