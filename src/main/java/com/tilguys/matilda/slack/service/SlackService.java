package com.tilguys.matilda.slack.service;

import com.tilguys.matilda.til.domain.Tag;
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

    // 웹훅 URL 대신 Slack API URL 사용
    @Value("${slack.post.message.url}")
    private String SLACK_POST_MESSAGE_URL;

    // 슬랙 OAuth 토큰 추가 필요
    @Value("${slack.bot.token}")
    private String slackBotToken;

    // 알림을 보낼 채널 ID 추가 필요
    @Value("${slack.channel.id}")
    private String channelId;

    public void sendTilWriteAlarm(String content, String nickname, String dateString, List<Tag> tags) {
        Map<String, Object> payload = new HashMap<>();
        List<String> tagStrings = tags.stream().map(Tag::getTagString).toList();
        SlackAlarmBlock slackAlarmBlock = new SlackAlarmBlock(content, nickname, dateString, tagStrings);
        List<Map<String, Object>> blocks = slackAlarmBlock.alarmBlock();

        // chat.postMessage API에 필요한 파라미터 추가
        payload.put("channel", channelId);
        payload.put("blocks", blocks);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Bearer 토큰 인증 방식 추가
        headers.setBearerAuth(slackBotToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        restTemplate.postForEntity(SLACK_API_URL, entity, String.class);
    }
}
