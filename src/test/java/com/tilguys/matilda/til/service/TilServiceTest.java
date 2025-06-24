package com.tilguys.matilda.til.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilDefinitionRequest;
import com.tilguys.matilda.til.repository.TilRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TilServiceTest {

    @Autowired
    private TilService tilService;

    @Autowired
    private TilRepository tilRepository;

    @Nested
    class TIL_업데이트_테스트 {

        @Test
        void 정상적으로_TIL이_업데이트된다() {
            // given
            long userId = 1L;
            Til til = createTestTilFixture();
            til = tilRepository.save(til);

            TilDefinitionRequest request = new TilDefinitionRequest(
                    "new title", "new content", LocalDate.of(2024, 6, 2), true
            );

            // when
            tilService.updateTil(til.getTilId(), request, userId);

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
            long notExistId = 9999L;
            long userId = 1L;
            TilDefinitionRequest request = new TilDefinitionRequest(
                    "title", "content", LocalDate.now(), true
            );

            // when && then
            assertThatThrownBy(() -> tilService.updateTil(notExistId, request, userId))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 삭제된_TIL은_업데이트되지_않는다() {
            // given
            Til til = createTestTilFixture();
            long userId = 1L;

            til.markAsDeleted();
            tilRepository.save(til);

            TilDefinitionRequest request = new TilDefinitionRequest(
                    "new title", "new content", LocalDate.of(2024, 6, 2), true
            );

            // when && then
            assertThatThrownBy(() -> tilService.updateTil(til.getTilId(), request, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("삭제된 TIL은 수정할 수 없습니다.");
        }
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
