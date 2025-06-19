package com.tilguys.matilda.til.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilUpdateRequest;
import com.tilguys.matilda.til.repository.TilRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TilServiceTest {

    @Autowired
    private TilService tilService;

    @Autowired
    private TilRepository tilRepository;

    @Test
    void 정상적으로_TIL이_업데이트된다() {
        // given
        Til til = createTestTilFixture();
        til = tilRepository.save(til);

        TilUpdateRequest request = new TilUpdateRequest(
                "new content", LocalDate.of(2024, 6, 2), true, "new title"
        );

        // when
        tilService.updateTil(til.getTilId(), request);

        // then
        Til updated = tilRepository.findById(til.getTilId()).orElseThrow();
        assertThat(updated.getContent()).isEqualTo("new content");
        assertThat(updated.isPublic()).isTrue();
        assertThat(updated.getDate()).isEqualTo(LocalDate.of(2024, 6, 2));
        assertThat(updated.getTitle()).isEqualTo("new title");
    }

    @Test
    void 존재하지_않는_TIL을_업데이트하면_예외가_발생한다() {
        // given
        Long notExistId = 9999L;
        TilUpdateRequest request = new TilUpdateRequest(
                "content", LocalDate.now(), true, "title"
        );

        // when & then
        assertThatThrownBy(() -> tilService.updateTil(notExistId, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 삭제된_TIL은_업데이트되지_않는다() {
        // given
        Til til = createTestTilFixture();

        til.markAsDeleted();
        tilRepository.save(til);

        TilUpdateRequest request = new TilUpdateRequest(
                "new content", LocalDate.of(2024, 6, 2), true, "new title"
        );

        // when && then
        assertThatThrownBy(() -> tilService.updateTil(til.getTilId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제된 TIL은 수정할 수 없습니다.");
    }

    private Til createTestTilFixture() {
        return Til.builder()
                .content("content")
                .date(LocalDate.of(2024, 6, 1))
                .title("title")
                .isPublic(false)
                .build();
    }
}
