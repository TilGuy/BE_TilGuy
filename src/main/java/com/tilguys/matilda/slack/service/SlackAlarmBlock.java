package com.tilguys.matilda.slack.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlackAlarmBlock {

    private static final String TITLE_FORMAT = "✏️ %s\n";
    private static final String WRITER_FORMAT = "작성자: *%s*";
    private static final String CONTENT_FORMAT = "📖 내용 요약: %s\n";
    private static final String TAG_FORMAT = "🏷️ 태그 : %s\n";
    private static final String MARK_DOWN_TYPE = "mrkdwn";

    private final List<Map<String, Object>> blocks = new ArrayList<>();

    public SlackAlarmBlock(String content, String nickname, String dateString, List<String> tags) {
        // Header (제목)
        blocks.add(Map.of(
                "type", "header",
                "text", Map.of(
                        "type", "plain_text",
                        "text", String.format(TITLE_FORMAT, dateString),
                        "emoji", true
                )
        ));

        // 작성자 (작성자 정보 바로 제목 다음)
        blocks.add(Map.of(
                "type", "context",
                "elements", List.of(
                        Map.of(
                                "type", MARK_DOWN_TYPE,
                                "text", String.format(WRITER_FORMAT, nickname)
                        )
                )
        ));

        blocks.add(Map.of(
                "type", "section",
                "fields", List.of(
                        Map.of(
                                "type", MARK_DOWN_TYPE,
                                "text", String.format(CONTENT_FORMAT, content)
                        )
                )
        ));

        String tagString = String.join(", ", tags);
        blocks.add(Map.of(
                "type", "section",
                "fields", List.of(
                        Map.of(
                                "type", MARK_DOWN_TYPE,
                                "text", String.format(TAG_FORMAT, tagString)
                        )
                )
        ));
    }

    public List<Map<String, Object>> alarmBlock() {
        return blocks;
    }
}
