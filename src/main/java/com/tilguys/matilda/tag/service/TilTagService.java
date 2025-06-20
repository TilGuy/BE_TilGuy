package com.tilguys.matilda.tag.service;

import com.tilguys.matilda.common.external.OpenAIClient;
import com.tilguys.matilda.tag.domain.SubTag;
import com.tilguys.matilda.tag.domain.TilTagGenerator;
import com.tilguys.matilda.tag.domain.TilTagParser;
import com.tilguys.matilda.tag.domain.TilTags;
import com.tilguys.matilda.tag.repository.SubTagRepository;
import com.tilguys.matilda.tag.repository.TagRepository;
import com.tilguys.matilda.til.domain.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TilTagService {

    private final OpenAIClient openAIClient;
    private final TilTagGenerator tagGenerator;
    private final TilTagParser tagParser;
    private final TagRepository tagRepository;
    private final SubTagRepository subTagRepository;

    public TilTagService(@Autowired OpenAIClient openAIClient,
                         TagRepository tagRepository, SubTagRepository subTagRepository) {
        this.tagRepository = tagRepository;
        this.subTagRepository = subTagRepository;
        this.tagGenerator = new TilTagGenerator();
        this.tagParser = new TilTagParser();
        this.openAIClient = openAIClient;
    }

    public String requestTilTagResponseJson(String tilContent) {
        return openAIClient.callOpenAI(
                tagGenerator.createPrompt(tilContent),
                tagGenerator.createFunctionDefinition()
        );
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
    public List<SubTag> saveSubTags(String tilResponseJson, TilTags tilTags) {
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
