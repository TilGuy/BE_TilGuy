package com.tilguys.matilda.til.domain;

import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TilTest {

    @ParameterizedTest
    @CsvSource({
            "2025-02-09, false",
            "2025-02-10, true",
            "2025-02-12, true",
            "2025-02-14, true",
            "2025-02-15, false",
    })
    void 날짜_범위에_해당_되는지_판단한다(final LocalDate date, final boolean expected) {
        // given
        LocalDate from = LocalDate.of(2025, 2, 10);
        LocalDate to = LocalDate.of(2025, 2, 14);

        Til til = Til.builder()
                .date(date)
                .build();

        // when
        boolean result = til.isWithinDateRange(from, to);

        // then
        assertThat(result)
                .isEqualTo(expected);
    }
}
