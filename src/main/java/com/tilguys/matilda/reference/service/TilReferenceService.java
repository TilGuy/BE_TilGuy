package com.tilguys.matilda.reference.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilguys.matilda.common.external.OpenAIClient;
import com.tilguys.matilda.reference.domain.TilReferenceGenerator;
import com.tilguys.matilda.reference.domain.TilReferenceParser;
import com.tilguys.matilda.til.domain.Reference;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TilReferenceService {

    private final OpenAIClient openAIClient;
    private final TilReferenceGenerator tilReferenceGenerator;
    private final TilReferenceParser tilReferenceParser;

    public TilReferenceService(@Value(value = "${openai.api.key}") String apiKey,
                               @Value(value = "${openai.api.url}") String apiUrl,
                               ObjectMapper objectMapper
    ) {
        this.openAIClient = new OpenAIClient(apiKey, apiUrl);
        this.tilReferenceGenerator = new TilReferenceGenerator();
        this.tilReferenceParser = new TilReferenceParser(objectMapper);
    }

    public List<Reference> extractTilReference(String tilContent) {
        String responseJson = openAIClient.callOpenAI(
                tilReferenceGenerator.createPrompt(tilContent),
                tilReferenceGenerator.createFunctionDefinition()
        );
        return tilReferenceParser.parseReferences(responseJson);
    }
}
