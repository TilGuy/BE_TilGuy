package com.tilguys.matilda.study.slack;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.tilguys.matilda.slack.service.SlackService;
import com.tilguys.matilda.til.domain.Tag;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@ActiveProfiles("production")
class SlackServiceTest {

    @Autowired
    private SlackService slackService;

    @Test
    void 설정_파일에_등록된_채널로_슬랙_알람을_보낼수_있다() {
        List<Tag> tags = List.of(new Tag(null, "카프카", null), new Tag(null, "객체지향", null));

        assertDoesNotThrow(() -> slackService.sendTilWriteAlarm(
                "오늘은 Kafka를 공부했다.",
                "praisebak -> 투다로 뜨게할 방법이 없을까 - config에 nickname 넣자!",
                "2021-01-24 월요일 - 이부분은 컨트롤러에서 전달해줄것 - ",
                tags
        ));
    }
}
