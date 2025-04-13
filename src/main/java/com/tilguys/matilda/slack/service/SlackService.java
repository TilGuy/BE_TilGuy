package com.tilguys.matilda.slack.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class SlackService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    public void sendTilWriteAlarm(String content, String nickname, String dateString, List<String> tags) {
        Map<String, Object> payload = new HashMap<>();
        SlackAlarmBlock slackAlarmBlock = new SlackAlarmBlock(content, nickname, dateString, tags);
        List<Map<String, Object>> blocks = slackAlarmBlock.alarmBlock();
        payload.put("blocks", blocks);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        restTemplate.postForEntity(webhookUrl, entity, String.class);
    }
}
