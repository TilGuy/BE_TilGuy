package com.tilguys.matilda.til.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.domain.TilFixture;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.TilUserFixture;
import com.tilguys.matilda.user.repository.UserRepository;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
class TilRepositoryTest {

    @Autowired
    private TilRepository tilRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 삭제되지_않은_공개_TIL_목록을_페이징하여_조회한다() {
        // given
        TilUser tilUser = createAndSaveTilUser();

        // 조회 가능
        createTils(tilUser, true, false, 15);

        // 조회 불가능 (isPublic : false)
        createTils(tilUser, false, false, 5);

        // 조회 불가능 (isDeleted : true)
        createTils(tilUser, true, true, 5);

        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<Til> result = tilRepository.findAllByIsPublicTrueAndIsDeletedFalse(pageRequest);

        // then
        assertAll(
                () -> assertThat(result.getTotalElements()).isEqualTo(15),
                () -> assertThat(result.getTotalPages()).isEqualTo(2),
                () -> assertThat(result).extracting("isPublic").containsOnly(true),
                () -> assertThat(result).extracting("isDeleted").containsOnly(false)
        );
    }

    private void createTils(TilUser tilUser, boolean isPublic, boolean isDeleted, int count) {
        IntStream.range(0, count).forEach(i ->
                createAndSaveTil(tilUser, isPublic, isDeleted)
        );
    }

    private void createAndSaveTil(TilUser tilUser, boolean isPublic, boolean isDeleted) {
        Til til = TilFixture.createTilFixture(tilUser, isPublic, isDeleted);
        tilRepository.save(til);
    }

    private TilUser createAndSaveTilUser() {
        TilUser tilUser = TilUserFixture.createTilUserFixture();
        userRepository.save(tilUser);
        return tilUser;
    }
}
