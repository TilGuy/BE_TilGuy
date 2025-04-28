package com.tilguys.matilda.study.ai;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.tilguys.matilda.quiz.service.QuizService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@ActiveProfiles("test")
class QuizServiceTest {

    @Autowired
    private QuizService quizService;

    @Test
    void gpt_실제_할당량_소모_요청_테스트() {
        assertDoesNotThrow(() -> quizService.generateTilQuiz("오늘은 Kafka를 공부했다."));
    }
}
