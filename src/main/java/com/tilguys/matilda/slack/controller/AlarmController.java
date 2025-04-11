package com.tilguys.matilda.slack.controller;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


//TODO config에서 권한설정해야함
@RestController
@RequestMapping("/api/slack/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    @GetMapping("/hi")
    public ResponseEntity<?> sendSlackMessage() {
        Map<String, String> payload = new HashMap<>();
        payload.put("text", "https://github.com/TilGuy/BE_TilGuy/issues/30");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

        restTemplate.postForEntity(webhookUrl, entity, String.class);

        return ResponseEntity.ok().build();
    }
}
