package com.tilguys.matilda.reference.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilguys.matilda.common.external.exception.OpenAIException;
import com.tilguys.matilda.til.domain.Reference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TilReferenceParser {

    private final ObjectMapper objectMapper;

    public List<Reference> parseReferences(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            String arguments = root.path("choices").get(0)
                    .path("message").path("tool_calls").get(0)
                    .path("function").path("arguments").asText();

            JsonNode argsNode = objectMapper.readTree(arguments);

            JsonNode referencesNode = argsNode.get("references");

            Set<Reference> referenceSet = new HashSet<>();

            for (JsonNode referenceNode : referencesNode) {
                String word = referenceNode.get("word").asText();
                String referenceText = referenceNode.get("info").asText();

                Reference reference = Reference.builder()
                        .word(word.toLowerCase().trim())
                        .info(referenceText.trim())
                        .build();

                referenceSet.add(reference);
            }

            return new ArrayList<>(referenceSet);

        } catch (JsonProcessingException e) {
            throw new OpenAIException("Failed to process reference extraction response: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new OpenAIException("Unexpected response structure from OpenAI API" + e.getMessage());
        }
    }
}
