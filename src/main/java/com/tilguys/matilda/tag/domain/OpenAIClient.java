package com.tilguys.matilda.tag.domain;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class OpenAIClient {

    private final String apiUrl;
    private final String apiKey;
    private final RestTemplate restTemplate;

    public OpenAIClient(String apiKey, String apiUrl, RestTemplate restTemplate) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.restTemplate = restTemplate;
    }

    public String callOpenAI(List<Map<String, Object>> messages, Map<String, Object> functionDefinition) {
        Map<String, Object> tool = Map.of(
                "type", "function",
                "function", functionDefinition
        );

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-2024-05-13",
                "messages", messages,
                "tools", List.of(tool),
                "tool_choice", Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", functionDefinition.get("name")
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

        return response.getBody();
    }
}
