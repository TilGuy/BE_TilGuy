package com.tilguys.matilda.til.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.tilguys.matilda.tag.repository.SubTagRepository;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilReadAllResponse;
import com.tilguys.matilda.til.repository.TilRepository;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class RecentTilServiceTest {

    private static TilUser tilUser;

    @Autowired
    private RecentTilService recentTilService;

    @Autowired
    private TilRepository tilRepository;

    @Autowired
    private SubTagRepository subTagRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void beforeAll(@Autowired UserRepository userRepository) {
        tilUser = TilUser.builder()
                .providerInfo(ProviderInfo.GITHUB)
                .role(Role.USER)
                .nickname("test")
                .identifier("test")
                .build();
        userRepository.save(tilUser);
    }

    @BeforeEach
    void tearDown() {
        subTagRepository.deleteAll();
        tilRepository.deleteAll();
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "10, 10",
            "15, 10"
    })
    void 최근_TIL중_10개만_반환한다(int tilCreateCount, int expectedSize) {
        // given
        for (int i = 0; i < tilCreateCount; i++) {
            Til til = createTestTilFixture();
            tilRepository.save(til);
        }

        // when
        List<TilReadAllResponse> result = recentTilService.getRecentTils();

        // then
        assertThat(result).hasSize(expectedSize);
    }

    @Test
    void 최근_TIL_없으면_빈_리스트를_반환한다() {
        // when
        List<TilReadAllResponse> result = recentTilService.getRecentTils();

        // then
        assertThat(result).hasSize(0);
    }

    @Test
    void 최근_TIL을_createAt_기준으로_반환한다() {
        // given
        Long tilId1 = 1L;
        Long tilId2 = 2L;
        Long tilId3 = 3L;

        LocalDateTime dateTime1 = LocalDateTime.of(2024, 9, 1, 0, 0);
        LocalDateTime dateTime2 = LocalDateTime.of(2025, 6, 1, 0, 0);
        LocalDateTime dateTime3 = LocalDateTime.of(2025, 1, 1, 0, 0);

        insertTilFixtureWithDateTime(tilId1, dateTime1);
        insertTilFixtureWithDateTime(tilId2, dateTime2);
        insertTilFixtureWithDateTime(tilId3, dateTime3);

        // when
        List<Long> result = recentTilService.getRecentTils().stream()
                .map(TilReadAllResponse::id)
                .toList();

        // then
        assertThat(result)
                .containsExactly(
                        tilId2,
                        tilId3,
                        tilId1
                );
    }

    private void insertTilFixtureWithDateTime(Long tilId, LocalDateTime dateTime) {
        jdbcTemplate.update(
                "INSERT INTO til (til_id, user_id, title, content, date, is_public, is_deleted, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                tilId, tilUser.getId(), "제목", "내용", dateTime.toLocalDate(), true, false, dateTime
        );
    }

    private Til createTestTilFixture() {
        return Til.builder()
                .tilUser(tilUser)
                .title("Test title")
                .content("Test content")
                .date(LocalDate.now())
                .isPublic(true)
                .isDeleted(false)
                .build();
    }
}
