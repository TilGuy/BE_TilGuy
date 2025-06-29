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
public class OpenAIClient {

    private static final String FUNCTION = "function";
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl;
    private final String apiKey;

    public OpenAIClient(@Value(value = "${openai.api.key}") String apiKey,
                        @Value(value = "${openai.api.url}") String apiUrl) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public String callOpenAI(List<Map<String, Object>> messages, Map<String, Object> functionDefinition) {
        Map<String, Object> tool = Map.of(
                "type", FUNCTION,
                FUNCTION, functionDefinition
        );

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-2024-05-13",
                "messages", messages,
                "tools", List.of(tool),
                "tool_choice", Map.of(
                        "type", FUNCTION,
                        FUNCTION, Map.of(
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
