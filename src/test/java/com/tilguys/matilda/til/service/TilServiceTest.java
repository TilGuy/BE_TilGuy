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
import com.tilguys.matilda.til.dto.TilDatesResponse;
import com.tilguys.matilda.til.dto.TilDefinitionRequest;
import com.tilguys.matilda.til.dto.TilDetailsResponse;
import com.tilguys.matilda.til.dto.TilReadAllResponse;
import com.tilguys.matilda.til.repository.TilRepository;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@Transactional
class TilServiceTest {

    @Autowired
    private TilService tilService;

    @Autowired
    private TilRepository tilRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        tilUser = userRepository.save(tilUser);
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
            List<TilReadAllResponse> response = tilService.getPublicTils(null, null, 12);

            // then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(3);
        }

        @Test
        void 커서_기반_조회가_정상적으로_작동한다() {
            // given
            List<Til> publicTils = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                publicTils.add(createTestTilFixture(
                        true, false, LocalDate.of(2025, 1, 1).plusDays(i)));
            }
            tilRepository.saveAll(publicTils);

            // when
            List<TilReadAllResponse> response = tilService.getPublicTils(null, null, 5);

            // then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(5);
        }

        @Test
        void 공개된_TIL이_없으면_빈_목록이_반환된다() {
            // given
            Til privateTil = createTestTilFixture(false, false, LocalDate.now());
            Til deletedTil = createTestTilFixture(true, true, LocalDate.now());
            tilRepository.saveAll(List.of(privateTil, deletedTil));

            // when
            List<TilReadAllResponse> response = tilService.getPublicTils(null, null, 2);
            // then
            assertThat(response).isEmpty();
        }

        @Test
        void 날짜_기준_내림차순으로_정렬된다() {
            // given
            Til til1 = createTestTilFixture(true, false, LocalDate.now().minusDays(2));
            Til til2 = createTestTilFixture(true, false, LocalDate.now().minusDays(1));
            Til til3 = createTestTilFixture(true, false, LocalDate.now());
            tilRepository.saveAll(List.of(til1, til2, til3));

            // when
            List<TilReadAllResponse> response = tilService.getPublicTils(null, null, 3);

            // then
            assertThat(response).hasSize(3);
            assertThat(response.get(0).id()).isEqualTo(til3.getTilId());
            assertThat(response.get(1).id()).isEqualTo(til2.getTilId());
            assertThat(response.get(2).id()).isEqualTo(til1.getTilId());
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

    @Nested
    class TIL_날짜_조회_테스트 {

        @Test
        void 사용자의_TIL이_존재하면_날짜_목록을_반환한다() {
            // given
            LocalDate date1 = LocalDate.of(2024, 6, 1);
            LocalDate date2 = LocalDate.of(2024, 6, 2);
            Til til1 = createTestTilFixture(true, false, date1);
            Til til2 = createTestTilFixture(true, false, date2);
            tilRepository.saveAll(List.of(til1, til2));

            // when
            TilDatesResponse response = tilService.getAllTilDatesByUserId(tilUser.getId());

            // then
            assertThat(response.dates()).hasSize(2);
            assertThat(response.dates()).contains(date1, date2);
        }

        @Test
        void 사용자의_TIL이_없으면_빈_목록을_반환한다() {
            // when
            TilDatesResponse response = tilService.getAllTilDatesByUserId(tilUser.getId());

            // then
            assertThat(response.dates()).isEmpty();
        }

        @Test
        void 삭제된_TIL의_날짜는_반환되지_않는다() {
            // given
            LocalDate date1 = LocalDate.of(2024, 6, 1);
            LocalDate date2 = LocalDate.of(2024, 6, 2);
            Til til1 = createTestTilFixture(true, false, date1);
            Til til2 = createTestTilFixture(true, true, date2);
            tilRepository.saveAll(List.of(til1, til2));

            // when
            TilDatesResponse response = tilService.getAllTilDatesByUserId(tilUser.getId());

            // then
            assertThat(response.dates()).hasSize(1);
            assertThat(response.dates()).contains(date1);
            assertThat(response.dates()).doesNotContain(date2);
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
        void 업데이트_날짜에_이미_게시물이_존재하면_예외가_발생한다() {
            // given
            long userId = tilUser.getId();
            LocalDate existingDate = LocalDate.of(2024, 6, 2);
            LocalDate targetDate = LocalDate.of(2024, 6, 2);
            Til existingTil = createTestTilFixture(true, false, existingDate);
            Til tilToUpdate = createTestTilFixture(true, false, LocalDate.of(2024, 6, 1)); // 다른 날짜
            tilRepository.saveAll(List.of(existingTil, tilToUpdate));

            TilDefinitionRequest request = new TilDefinitionRequest(
                    "new title", "new content", targetDate, true
            );

            // when && then
            assertThatThrownBy(() -> tilService.updateTil(tilToUpdate.getTilId(), request, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 날짜에 이미 작성된 게시물이 존재합니다!");
        }

        @Test
        void 삭제된_TIL은_업데이트되지_않는다() {
            // given
            Til til = createTestTilFixture();
            long userId = tilUser.getId();
            til.markAsDeletedBy(userId);
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
    class TIL_삭제_테스트 {

        @Test
        void 정상적으로_TIL이_삭제된다() {
            // given
            Til til = createTestTilFixture(true, false, LocalDate.now());
            til = tilRepository.save(til);

            // when
            tilService.deleteTil(til.getTilId(), tilUser.getId());

            // then
            Til deletedTil = tilRepository.findById(til.getTilId()).orElseThrow();
            assertThat(deletedTil.isDeleted()).isTrue();
        }

        @Test
        void 존재하지_않는_TIL_ID로_삭제_시도_시_예외가_발생한다() {
            // given
            long nonExistentId = 9999L;

            // when && then
            assertThatThrownBy(() -> tilService.deleteTil(nonExistentId, 1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class 날짜_범위별_TIL_조회_테스트 {

        @Test
        void 날짜_범위_내에_TIL이_존재하면_해당_TIL만_반환된다() {
            // given
            LocalDate from = LocalDate.of(2024, 6, 1);
            LocalDate to = LocalDate.of(2024, 6, 3);
            Til til1 = createTestTilFixture(true, false, LocalDate.of(2024, 6, 2));
            Til til2 = createTestTilFixture(true, false, LocalDate.of(2024, 6, 4)); // 범위 밖
            tilRepository.saveAll(List.of(til1, til2));

            // when
            TilDetailsResponse response = tilService.getTilByDateRange(tilUser.getId(), from, to);

            // then
            assertThat(response.tils()).hasSize(1);
        }

        @Test
        void 날짜_범위_내에_TIL이_없으면_빈_목록이_반환된다() {
            // given
            LocalDate from = LocalDate.of(2024, 6, 1);
            LocalDate to = LocalDate.of(2024, 6, 3);
            Til til = createTestTilFixture(true, false, LocalDate.of(2024, 6, 4)); // 범위 밖
            tilRepository.save(til);

            // when
            TilDetailsResponse response = tilService.getTilByDateRange(tilUser.getId(), from, to);

            // then
            assertThat(response.tils()).isEmpty();
        }

        @Test
        void 삭제된_TIL은_반환되지_않는다() {
            // given
            LocalDate from = LocalDate.of(2024, 6, 1);
            LocalDate to = LocalDate.of(2024, 6, 3);
            Til til1 = createTestTilFixture(true, false, LocalDate.of(2024, 6, 2));
            Til til2 = createTestTilFixture(true, true, LocalDate.of(2024, 6, 2)); // 삭제됨
            tilRepository.saveAll(List.of(til1, til2));

            // when
            TilDetailsResponse response = tilService.getTilByDateRange(tilUser.getId(), from, to);

            // then
            assertThat(response.tils()).hasSize(1);
            assertThat(response.tils().stream().noneMatch(detail -> detail.tilId().equals(til2.getTilId()))).isTrue();
        }
    }

    @Nested
    class TIL_ID로_TIL_조회_테스트 {

        @Test
        void TIL_ID가_존재하면_해당_TIL이_반환된다() {
            // given
            Til til = createTestTilFixture(true, false, LocalDate.now());
            til = tilRepository.save(til);

            // when
            Til result = tilService.getTilByTilId(til.getTilId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTilId()).isEqualTo(til.getTilId());
        }

        @Test
        void TIL_ID가_존재하지_않으면_예외가_발생한다() {
            // given
            long nonExistentId = 9999L;

            // when && then
            assertThatThrownBy(() -> tilService.getTilByTilId(nonExistentId))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class 여러_TIL_ID로_TIL_조회_테스트 {

        @Test
        void 모든_ID에_해당하는_TIL이_존재하면_목록이_반환된다() {
            // given
            Til til1 = createTestTilFixture(true, false, LocalDate.now());
            Til til2 = createTestTilFixture(true, false, LocalDate.now());
            tilRepository.saveAll(List.of(til1, til2));
            List<Long> tilIds = List.of(til1.getTilId(), til2.getTilId());

            // when
            List<Til> result = tilService.getTilsByIds(tilIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(til1, til2);
        }

        @Test
        void 일부_ID가_존재하지_않으면_예외가_발생한다() {
            // given
            Til til = createTestTilFixture(true, false, LocalDate.now());
            til = tilRepository.save(til);
            List<Long> tilIds = List.of(til.getTilId(), 9999L); // 존재하지 않는 ID 포함

            // when && then
            assertThatThrownBy(() -> tilService.getTilsByIds(tilIds))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class 최근_작성된_TIL_조회_테스트 {

        @Test
        void 주어진_시간_이후_작성된_TIL이_존재하면_해당_TIL만_반환된다() {
            // given
            LocalDateTime startTime = LocalDateTime.of(2024, 6, 1, 0, 0);
            Long tilId1 = 1L;
            Long tilId2 = 2L;
            LocalDateTime dateTime1 = LocalDateTime.of(2024, 6, 2, 10, 0);
            LocalDateTime dateTime2 = LocalDateTime.of(2024, 5, 30, 10, 0);
            insertTilFixtureWithDateTime(tilId1, dateTime1);
            insertTilFixtureWithDateTime(tilId2, dateTime2);

            // when
            List<Til> result = tilService.getRecentWroteTil(startTime);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTilId()).isEqualTo(tilId1);
        }

        @Test
        void 주어진_시간_이후_작성된_TIL이_없으면_빈_목록이_반환된다() {
            // given
            LocalDateTime startTime = LocalDateTime.of(2024, 6, 1, 0, 0);
            Long tilId = 1L;
            LocalDateTime dateTime = LocalDateTime.of(2024, 5, 30, 10, 0);
            insertTilFixtureWithDateTime(tilId, dateTime);

            // when
            List<Til> result = tilService.getRecentWroteTil(startTime);

            // then
            assertThat(result).isEmpty();
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

    private void insertTilFixtureWithDateTime(Long tilId, LocalDateTime dateTime) {
        jdbcTemplate.update(
                "INSERT INTO til (til_id, user_id, title, content, date, is_public, is_deleted, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                tilId, tilUser.getId(), "제목", "내용", dateTime.toLocalDate(), true, false, dateTime
        );
    }
}
