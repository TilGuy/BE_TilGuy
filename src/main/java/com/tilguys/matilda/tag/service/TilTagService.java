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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class TilTagService {

    private final FailoverAIServiceManager failoverAIServiceManager;
    private final TilTagGenerator tagGenerator;
    private final TilTagParser tagParser;
    private final TagRepository tagRepository;
    private final SubTagRepository subTagRepository;
    private final TilService tilService;

    public TilTagService(
            @Autowired FailoverAIServiceManager failoverAIServiceManager,
            TagRepository tagRepository,
            SubTagRepository subTagRepository,
            TilService tilService
    ) {
        this.tagRepository = tagRepository;
        this.subTagRepository = subTagRepository;
        this.tagGenerator = new TilTagGenerator();
        this.tagParser = new TilTagParser();
        this.failoverAIServiceManager = failoverAIServiceManager;
        this.tilService = tilService;
    }

    public String requestTilTagResponseJson(String tilContent) {
        return failoverAIServiceManager.callAIWithSimpleFallback(
                tagGenerator.createPrompt(tilContent),
                tagGenerator.createFunctionDefinition()
        );
    }

    @Transactional
    public void createTags(TilCreatedEvent tilCreatedEvent) {
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
