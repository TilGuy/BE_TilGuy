package com.tilguys.matilda.til.domain;

import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class TilTest {
    
    @Test
    void 날짜_범위에_해당_되는지_판단한다() {
        // given
        LocalDate from = LocalDate.of(2025, 2, 10);
        LocalDate to = LocalDate.of(2025, 2, 14);
        LocalDate date = LocalDate.of(2025, 2, 12);

        Til til = Til.builder()
                .date(date)
                .build();

        // when
        boolean result = til.isWithinDateRange(from, to);

        // then
        assertThat(result)
                .isTrue();
    }
}
