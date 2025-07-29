package com.tilguys.matilda.common.external;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ClaudeClient implements AIClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl;
    private final String apiKey;

    public ClaudeClient(@Value(value = "${claude.api.key:}") String apiKey,
                        @Value(value = "${claude.api.url:https://api.anthropic.com/v1/messages}") String apiUrl) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    @Override
    public String callAI(List<Map<String, Object>> messages, Map<String, Object> functionDefinition) {
        // Claude API 형식에 맞게 변환
        Map<String, Object> body = Map.of(
                "model", "claude-3-haiku-20240307",
                "max_tokens", 1000,
                "messages", transformMessages(messages),
                "tools", List.of(Map.of(
                        "name", functionDefinition.get("name"),
                        "description", functionDefinition.get("description"),
                        "input_schema", functionDefinition.get("parameters")
                ))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

        return response.getBody();
    }

    @Override
    public String getClientName() {
        return "Claude";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isEmpty() && 
               apiUrl != null && !apiUrl.trim().isEmpty();
    }

    private List<Map<String, Object>> transformMessages(List<Map<String, Object>> messages) {
        // OpenAI 형식을 Claude 형식으로 변환
        return messages.stream()
                .map(message -> Map.of(
                        "role", message.get("role"),
                        "content", message.get("content")
                ))
                .toList();
    }
} 