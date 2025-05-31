package com.tilguys.matilda.tag.service;

import com.tilguys.matilda.common.external.OpenAIClient;
import com.tilguys.matilda.tag.domain.TilTagGenerator;
import com.tilguys.matilda.tag.domain.TilTagParser;
import com.tilguys.matilda.tag.repository.TagRepository;
import com.tilguys.matilda.til.domain.Tag;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TilTagService {

    private final OpenAIClient openAIClient;
    private final TilTagGenerator tagGenerator;
    private final TilTagParser tagParser;
    private final TagRepository tagRepository;


    public TilTagService(@Autowired OpenAIClient openAIClient,
                         TagRepository tagRepository) {
        this.tagRepository = tagRepository;
        this.tagGenerator = new TilTagGenerator();
        this.tagParser = new TilTagParser();
        this.openAIClient = openAIClient;
    }

    public List<Tag> extractTilTags(String tilContent) {
        String responseJson = openAIClient.callOpenAI(
                tagGenerator.createPrompt(tilContent),
                tagGenerator.createFunctionDefinition()
        );

        Set<String> tags = tagParser.parseTags(responseJson);
        return tags.stream()
                .map(Tag::new)
                .toList()
                .subList(0, Math.min(5, tags.size()));
    }

    public List<Tag> getRecentWroteTags(LocalDate recent) {
        return tagRepository.findByCreatedAtAfter(LocalDateTime.of(recent, LocalTime.MIN));
    }
}
