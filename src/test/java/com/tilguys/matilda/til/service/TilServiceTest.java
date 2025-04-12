package com.tilguys.matilda.til.service;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilDetailResponse;
import com.tilguys.matilda.til.dto.TilDetailsResponse;
import com.tilguys.matilda.til.repository.TilRepository;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TilServiceTest {

    @Mock
    private TilRepository tilRepository;

    private TilService tilService;

    @BeforeEach
    void setUp() {
        tilService = new TilService(tilRepository);
    }

    @Test
    void 날짜_범위에_포함되는_TIL만_반환한다() {
        // given
        Long userId = 1L;
        LocalDate from = LocalDate.of(2025, 2, 10);
        LocalDate to = LocalDate.of(2025, 2, 14);

        Til outOfRange = createTil(1L, 9);
        Til withinRange = createTil(2L, 12);

        List<Til> tils = List.of(outOfRange, withinRange);
        when(tilRepository.findByUserId(userId))
                .thenReturn(tils);

        // when
        TilDetailsResponse result = tilService.getTilByDateRange(userId, from, to);

        // then
        assertThat(result.tils())
                .hasSize(1)
                .contains(TilDetailResponse.fromEntity(withinRange));
    }

    private Til createTil(final long tilId, final int dayOfMonth) {
        return Til.builder()
                .tilId(tilId)
                .date(LocalDate.of(2025, 2, dayOfMonth))
                .build();
    }
}
