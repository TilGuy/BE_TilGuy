package com.tilguys.matilda.til.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    @Test
    void 삭제와_공개_조건의_최근_TIL을_조회한다() {
        // given
        TilUser tilUser = createAndSaveUserFixture();

        IntStream.range(0, 5).forEach(i -> {
            createTilFixture(true, false, tilUser);
        });

        createTilFixture(true, true, tilUser); // 삭제된 TIL
        createTilFixture(false, false, tilUser); // 비공개 TIL

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
        TilUser tilUser = createAndSaveUserFixture();

        IntStream.range(0, 15).forEach(i -> {
            createTilFixture(true, false, tilUser);
        });

        // when
        List<Til> result = tilRepository.findTop10ByIsDeletedFalseAndIsPublicTrueOrderByCreatedAtDesc();

        // then
        assertThat(result)
                .hasSize(10)
                .extracting("createdAt", LocalDateTime.class)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    private TilUser createAndSaveUserFixture() {
        TilUser tilUser = TilUser.builder()
                .providerInfo(ProviderInfo.GITHUB)
                .identifier("test-identifier")
                .role(Role.USER)
                .build();

        userRepository.save(tilUser);
        return tilUser;
    }

    private void createTilFixture(boolean isPublic, boolean isDeleted, TilUser tilUser) {
        tilRepository.save(
                Til.builder()
                        .tilUser(tilUser)
                        .isPublic(isPublic)
                        .isDeleted(isDeleted)
                        .build()
        );
    }
}
