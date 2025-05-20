package com.tilguys.matilda.til.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.tilguys.matilda.til.domain.Til;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class TilRepositoryTest {

    @Autowired
    private TilRepository tilRepository;

    @Test
    void 삭제와_공개_조건의_최근_TIL을_조회한다() {
        // given
        IntStream.range(0, 5).forEach(i -> {
            createTilFixture(true, false);
        });

        createTilFixture(true, true); // 삭제된 TIL
        createTilFixture(false, false); // 비공개 TIL

        // when
        List<Til> result = tilRepository.findTop10ByIsDeletedFalseAndIsPublicTrueOrderByCreatedAtDesc();

        // then
        assertThat(result)
                .hasSize(5)
                .extracting("createdAt", LocalDateTime.class)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    void 최근_TIL_10개를_조회한다() {
        // given
        IntStream.range(0, 15).forEach(i -> {
            createTilFixture(true, false);
        });

        // when
        List<Til> result = tilRepository.findTop10ByIsDeletedFalseAndIsPublicTrueOrderByCreatedAtDesc();

        // then
        assertThat(result)
                .hasSize(10)
                .extracting("createdAt", LocalDateTime.class)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    private void createTilFixture(boolean isPublic, boolean isDeleted) {
        tilRepository.save(
                Til.builder()
                        .userId(1L)
                        .isPublic(isPublic)
                        .isDeleted(isDeleted)
                        .build()
        );
    }
}
