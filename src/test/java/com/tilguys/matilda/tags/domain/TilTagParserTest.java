package com.tilguys.matilda.tags.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.tilguys.matilda.tag.domain.TilTagParser;
import com.tilguys.matilda.tag.domain.TilTags;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest
@ActiveProfiles("test")
class TilTagParserTest {

    @Test
    void 요청이_json이_아니면_유효하지_않으면_예외가_발생한다() {
        TilTagParser tilTagParser = new TilTagParser();
        Assertions.assertAll(
                () -> assertThatThrownBy(() -> tilTagParser.parseTags("not json")),
                () -> assertThatThrownBy(() -> tilTagParser.parseSubTags("not json",
                        new TilTags(new ArrayList<>()))).doesNotThrowAnyException()
        );
    }
}
