package com.tilguys.matilda.til.service;

import static com.tilguys.matilda.user.TilUserFixture.createTilUserFixture;
import static org.assertj.core.api.Assertions.assertThat;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.domain.TilFixture;
import com.tilguys.matilda.til.dto.TilWithUserResponses;
import com.tilguys.matilda.til.repository.TilRepository;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class TilServiceIntegrationTest {

    @Autowired
    private TilService tilService;

    @Autowired
    private TilRepository tilRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 공개된_TIL_목록을_페이지네이션하여_조회할_수_있다() {
        // given
        TilUser tilUser = createTilUserFixture();
        userRepository.save(tilUser);
        createAndSaveTils(tilUser, 15);

        int pageNumber = 0;
        int pageSize = 10;

        // when
        TilWithUserResponses result = tilService.getPublicTils(pageNumber, pageSize);

        // then
        assertThat(result.tilWithUsers()).hasSize(10);
    }

    private void createAndSaveTils(TilUser tilUser, int count) {
        IntStream.range(0, count).forEach(i -> {
            Til til = TilFixture.createTilFixture(tilUser);
            tilRepository.save(til);
        });
    }
}


