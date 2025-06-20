package com.tilguys.matilda.tag.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilguys.matilda.common.external.exception.OpenAIException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TilTagParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Set<String> parseTags(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            // tool_calls에서 arguments를 가져옴
            String arguments = root.path("choices").get(0)
                    .path("message").path("tool_calls").get(0)
                    .path("function").path("arguments").asText();

            // arguments는 JSON 문자열이므로 이를 파싱
            JsonNode argsNode = objectMapper.readTree(arguments);
            JsonNode tagsNode = argsNode.get("tags");

            // 반환할 태그 세트 생성
            Set<String> tagSet = new HashSet<>();

            // JsonNode 배열을 순회하면서 태그 추가
            for (JsonNode tagNode : tagsNode) {
                tagSet.add(tagNode.asText().toLowerCase());
            }

            return tagSet;
        } catch (JsonProcessingException e) {
            throw new OpenAIException("Failed to process tag extraction response");
        }
    }

    public List<SubTag> parseSubTags(String responseJson, TilTags coreTags) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            // tool_calls에서 arguments를 가져옴
            String arguments = root.path("choices").get(0)
                    .path("message").path("tool_calls").get(0)
                    .path("function").path("arguments").asText();

            // arguments는 JSON 문자열이므로 이를 파싱
            JsonNode argsNode = objectMapper.readTree(arguments);
            JsonNode tagsNode = argsNode.get("subTags");

            // 반환할 태그 세트 생성
            List<SubTag> subTags = new ArrayList<>();

            if (tagsNode == null) {
                return new ArrayList<>();
            }
            // JsonNode 배열을 순회하면서 태그 추가
            for (JsonNode node : tagsNode) {
                String coreTagName = node.get("coreTag").asText();
                String subTagName = node.get("subTag").asText();

                subTags.add(new SubTag(coreTags.findTag(coreTagName), subTagName));
            }

            return subTags;
        } catch (JsonProcessingException e) {
            throw new OpenAIException("Failed to process tag extraction response");
        }
    }
}
