package com.tilguys.matilda.tag.service;

import com.tilguys.matilda.tag.domain.OpenAIClient;
import com.tilguys.matilda.tag.domain.TilTagGenerator;
import com.tilguys.matilda.tag.domain.TilTagParser;
import com.tilguys.matilda.til.domain.Tag;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TilTagService {

    private final OpenAIClient openAIClient;
    private final TilTagGenerator tagGenerator;
    private final TilTagParser tagParser;

    public TilTagService(@Value(value = "${openai.api.key}") String apiKey,
                         @Value(value = "${openai.api.url}") String apiUrl,
                         @Autowired RestTemplate restTemplate) {
        this.openAIClient = new OpenAIClient(apiKey, apiUrl, restTemplate);
        this.tagGenerator = new TilTagGenerator();
        this.tagParser = new TilTagParser();
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
}
