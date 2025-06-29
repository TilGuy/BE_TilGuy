package com.tilguys.matilda.til.service;

import static com.tilguys.matilda.user.TilUserFixture.createTilUserFixture;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.tilguys.matilda.tag.repository.SubTagRepository;
import com.tilguys.matilda.tag.repository.TagRelationRepository;
import com.tilguys.matilda.tag.repository.TagRepository;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.domain.TilFixture;
import com.tilguys.matilda.til.dto.TilReadAllResponse;
import com.tilguys.matilda.til.repository.TilRepository;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class TilServiceIntegrationTest {

    @Autowired
    private TilService tilService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TilRepository tilRepository;

    @Autowired
    private SubTagRepository subTagRepository;

    @Autowired
    private TagRelationRepository tagRelationRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        subTagRepository.deleteAll();
        tagRelationRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();
        tilRepository.deleteAll();

        subTagRepository.flush();
        tagRepository.flush();
        userRepository.flush();
        tilRepository.flush();
    }

    @Test
    void 공개된_TIL_목록을_커서_단위로_조회할_수_있다() {
        // given
        TilUser tilUser = createTilUserFixture();
        userRepository.save(tilUser);
        createAndSaveTils(tilUser, 15);

        int size = 12;
        // when
        List<TilReadAllResponse> result = tilService.getPublicTils(null, null, size);

        // then
        assertThat(result.size()).isEqualTo(12L);
    }

    private void createAndSaveTils(TilUser tilUser, int count) {
        IntStream.range(0, count).forEach(i -> {
            Til til = TilFixture.createTilFixture(tilUser);
            tilRepository.save(til);
        });
    }
}


