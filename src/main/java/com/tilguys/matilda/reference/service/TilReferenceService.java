package com.tilguys.matilda.reference.service;

import com.tilguys.matilda.reference.domain.TilReferenceGenerator;
import com.tilguys.matilda.reference.domain.TilReferenceParser;
import com.tilguys.matilda.tag.domain.OpenAIClient;
import com.tilguys.matilda.til.domain.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class TilReferenceService {

    private final OpenAIClient openAIClient;
    private final TilReferenceGenerator tilReferenceGenerator;
    private final TilReferenceParser tilReferenceParser;

    public TilReferenceService(@Value(value = "${openai.api.key}") String apiKey,
                               @Value(value = "${openai.api.url}") String apiUrl,
                               @Autowired RestTemplate restTemplate) {
        this.openAIClient = new OpenAIClient(apiKey, apiUrl, restTemplate);
        this.tilReferenceGenerator = new TilReferenceGenerator();
        this.tilReferenceParser = new TilReferenceParser();
    }

    public List<Reference> extractTilReference(String tilContent) {
        String responseJson = openAIClient.callOpenAI(
                tilReferenceGenerator.createPrompt(tilContent),
                tilReferenceGenerator.createFunctionDefinition()
        );
        return tilReferenceParser.parseReferences(responseJson);
    }
}
