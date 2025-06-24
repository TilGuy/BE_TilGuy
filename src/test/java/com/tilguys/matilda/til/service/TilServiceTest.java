package com.tilguys.matilda.til.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.tilguys.matilda.reference.service.TilReferenceService;
import com.tilguys.matilda.tag.repository.SubTagRepository;
import com.tilguys.matilda.tag.repository.TagRepository;
import com.tilguys.matilda.tag.service.TilTagService;
import com.tilguys.matilda.til.domain.Reference;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.PagedTilResponse;
import com.tilguys.matilda.til.dto.TilDefinitionRequest;
import com.tilguys.matilda.til.repository.TilRepository;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class TilServiceTest {

    @Autowired
    private TilService tilService;

    @Autowired
    private TilRepository tilRepository;

    @Autowired
    private UserRepository userRepository;
    
    @MockitoBean
    private TilTagService tilTagService;

    @MockitoBean
    private TilReferenceService tilReferenceService;

    private TilUser tilUser;

    @BeforeEach
    void setUp() {
        tilUser = TilUser.builder()
                .providerInfo(ProviderInfo.GITHUB)
                .role(Role.USER)
                .nickname("test")
                .identifier("test")
                .build();
        userRepository.save(tilUser);
    }

    @AfterEach
    void tearDown() {
        tilRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    class 공개_TIL_조회_테스트 {

        @Test
        void 공개되고_삭제되지_않은_TIL만_반환된다() {
            // given
            Til publicTil1 = createTestTilFixture(true, false, LocalDate.now());
            Til publicTil2 = createTestTilFixture(true, false, LocalDate.now().minusDays(1));
            Til publicTil3 = createTestTilFixture(true, false, LocalDate.now().minusDays(2));
            Til privateTil = createTestTilFixture(false, false, LocalDate.now());
            Til deletedTil = createTestTilFixture(true, true, LocalDate.now());
            tilRepository.saveAll(List.of(publicTil1, publicTil2, publicTil3, privateTil, deletedTil));

            // when
            PagedTilResponse response = tilService.getPublicTils(0, 10);

            // then
            assertThat(response).isNotNull();
            assertThat(response.tils()).hasSize(3);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.currentPage()).isEqualTo(0);
        }

        @Test
        void 페이지네이션이_정상적으로_작동한다() {
            // given
            List<Til> publicTils = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                publicTils.add(createTestTilFixture(true, false, LocalDate.now().minusDays(i)));
            }
            tilRepository.saveAll(publicTils);

            // when
            PagedTilResponse response = tilService.getPublicTils(0, 2);

            // then
            assertThat(response.tils()).hasSize(2);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.currentPage()).isEqualTo(0);
        }

        @Test
        void 공개된_TIL이_없으면_빈_목록이_반환된다() {
            // given
            Til privateTil = createTestTilFixture(false, false, LocalDate.now());
            Til deletedTil = createTestTilFixture(true, true, LocalDate.now());
            tilRepository.saveAll(List.of(privateTil, deletedTil));

            // when
            PagedTilResponse response = tilService.getPublicTils(0, 10);

            // then
            assertThat(response.tils()).isEmpty();
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        void 날짜_기준_내림차순으로_정렬된다() {
            // given
            Til til1 = createTestTilFixture(true, false, LocalDate.now().minusDays(2));
            Til til2 = createTestTilFixture(true, false, LocalDate.now().minusDays(1));
            Til til3 = createTestTilFixture(true, false, LocalDate.now());
            tilRepository.saveAll(List.of(til1, til2, til3));

            // when
            PagedTilResponse response = tilService.getPublicTils(0, 10);

            // then
            assertThat(response.tils()).hasSize(3);
            assertThat(response.tils().get(0).id()).isEqualTo(til3.getTilId());
            assertThat(response.tils().get(1).id()).isEqualTo(til2.getTilId());
            assertThat(response.tils().get(2).id()).isEqualTo(til1.getTilId());
        }
    }

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

    @Nested
    class TIL_생성_테스트 {

        @AfterEach
        void tearDown(@Autowired SubTagRepository subTagRepository, @Autowired TagRepository tagRepository) {
            subTagRepository.deleteAll();
            tagRepository.deleteAll();
        }

        @Test
        void 정상적으로_TIL이_생성된다() {
            // given
            long userId = tilUser.getId();
            LocalDate testDate = LocalDate.of(2024, 6, 1);
            TilDefinitionRequest request = new TilDefinitionRequest(
                    "Test Title", "Test Content", testDate, true
            );

            List<Tag> mockTags = Arrays.asList(
                    Tag.builder().tagString("tag1").build(),
                    Tag.builder().tagString("tag2").build()
            );
            List<Reference> mockReferences = Arrays.asList(
                    Reference.builder().word("ref1").info("Reference 1 info").build(),
                    Reference.builder().word("ref2").info("Reference 2 info").build()
            );

            given(tilTagService.requestTilTagResponseJson(anyString())).willReturn("mockJsonResponse");
            given(tilTagService.saveTilTags(anyString())).willReturn(mockTags);
            given(tilReferenceService.extractTilReference(anyString())).willReturn(mockReferences);

            // when
            Til createdTil = tilService.createTil(request, userId);

            // then
            assertThat(createdTil).isNotNull();
            assertThat(createdTil.getTitle()).isEqualTo("Test Title");
            assertThat(createdTil.getContent()).isEqualTo("Test Content");
            assertThat(createdTil.getDate()).isEqualTo(testDate);
            assertThat(createdTil.isPublic()).isTrue();
            assertThat(createdTil.getTags()).containsExactlyElementsOf(mockTags);
            assertThat(createdTil.getReferences()).containsExactlyElementsOf(mockReferences);
        }

        @Test
        void 동일_날짜에_TIL이_존재하면_예외가_발생한다() {
            // given
            long userId = tilUser.getId();
            LocalDate testDate = LocalDate.of(2024, 6, 1);
            Til existingTil = createTestTilFixture(true, false, testDate);
            tilRepository.save(existingTil);

            TilDefinitionRequest request = new TilDefinitionRequest(
                    "New Title", "New Content", testDate, true
            );

            // when && then
            assertThatThrownBy(() -> tilService.createTil(request, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("같은 날에 작성된 게시물이 존재합니다!");
        }

        @Test
        void 태그가_추출되고_TIL에_저장된다() {
            // given
            long userId = tilUser.getId();
            LocalDate testDate = LocalDate.of(2024, 6, 1);
            TilDefinitionRequest request = new TilDefinitionRequest(
                    "Test Title", "Test Content with Tags", testDate, true
            );

            List<Tag> mockTags = Arrays.asList(
                    Tag.builder().tagString("java").build(),
                    Tag.builder().tagString("spring").build()
            );

            given(tilTagService.requestTilTagResponseJson(anyString())).willReturn("mockJsonResponse");
            given(tilTagService.saveTilTags(anyString())).willReturn(mockTags);

            // when
            Til createdTil = tilService.createTil(request, userId);

            // then
            assertThat(createdTil.getTags()).hasSize(2);
            assertThat(createdTil.getTags()).containsExactlyElementsOf(mockTags);
        }

        @Test
        void 참조가_추출되고_TIL에_저장된다() {
            // given
            long userId = tilUser.getId();
            LocalDate testDate = LocalDate.of(2024, 6, 1);
            TilDefinitionRequest request = new TilDefinitionRequest(
                    "Test Title", "Test Content with References", testDate, true
            );

            List<Reference> mockReferences = Arrays.asList(
                    Reference.builder().word("example").info("https://example.com").build(),
                    Reference.builder().word("test").info("https://test.com").build()
            );
            given(tilReferenceService.extractTilReference(anyString())).willReturn(mockReferences);

            // when
            Til createdTil = tilService.createTil(request, userId);

            // then
            assertThat(createdTil.getReferences()).hasSize(2);
            assertThat(createdTil.getReferences()).containsExactlyElementsOf(mockReferences);
        }
    }

    private Til createTestTilFixture() {
        return createTestTilFixture(false, false, LocalDate.of(2024, 6, 1));
    }

    private Til createTestTilFixture(boolean isPublic, boolean isDeleted, LocalDate date) {
        return Til.builder()
                .tilUser(tilUser)
                .content("content")
                .date(date)
                .title("title")
                .isPublic(isPublic)
                .isDeleted(isDeleted)
                .build();
    }
}
