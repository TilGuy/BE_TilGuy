package com.tilguys.matilda.reference.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tilguys.matilda.common.external.exception.OpenAIException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@ActiveProfiles("test")
class TilReferenceParserTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 요청이_json이_아니면_유효하지_않으면_예외가_발생한다() {
        TilReferenceParser tilReferenceParser = new TilReferenceParser(objectMapper);
        assertThatThrownBy(() -> tilReferenceParser.parseReferences("not json"))
                .isInstanceOf(OpenAIException.class)
                .hasMessageContaining("Failed to process reference extraction response:");
    }

    @Test
    void 요청이_null이면_예외가_발생한다() {
        TilReferenceParser tilReferenceParser = new TilReferenceParser(objectMapper);
        assertAll(
                () -> assertThatThrownBy(() -> tilReferenceParser.parseReferences(null))
                        .isInstanceOf(OpenAIException.class)
                        .hasMessageContaining("Response JSON is null or empty"),
                () -> assertThatThrownBy(() -> tilReferenceParser.parseReferences(""))
                        .isInstanceOf(OpenAIException.class)
                        .hasMessageContaining("Response JSON is null or empty")
        );
    }
}
