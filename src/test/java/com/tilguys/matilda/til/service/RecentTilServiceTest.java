package com.tilguys.matilda.til.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;

import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.dto.TilWithUserResponses;
import com.tilguys.matilda.til.repository.TilRepository;
import com.tilguys.matilda.user.TilUser;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecentTilServiceTest {

    @InjectMocks
    private RecentTilService recentTilService;

    @Mock
    private TilRepository tilRepository;

    @Test
    void 최근_TIL을_응답_객체로_반환한다() {
        // given
        TilUser tilUser = createTilUserFixture();
        List<Til> tils = List.of(createTilFixture("제목1", "내용1", tilUser), createTilFixture("제목2", "내용2", tilUser));

        doReturn(tils).when(tilRepository)
                .findTop10ByIsDeletedFalseAndIsPublicTrueOrderByCreatedAtDesc();

        // when
        TilWithUserResponses result = recentTilService.getRecentTils();

        // then
        assertAll(
                () -> assertThat(result.recents()).hasSize(2),
                () -> assertThat(result.recents()).extracting("title").containsOnly("제목1", "제목2"),
                () -> assertThat(result.recents()).extracting("content").containsOnly("내용1", "내용2"),
                () -> assertThat(result.recents()).extracting("nickname").containsOnly("이름", "이름"),
                () -> assertThat(result.recents()).extracting("avatarUrl").containsOnly("프로필 주소", "프로필 주소"),
                () -> assertThat(result.recents())
                        .extracting("tags.tags")
                        .allMatch(tags -> tags.equals(List.of("태그1", "태그2")))
        );
    }

    private TilUser createTilUserFixture() {
        return TilUser.builder()
                .nickname("이름")
                .avatarUrl("프로필 주소")
                .build();
    }

    private Til createTilFixture(String title, String content, TilUser tilUser) {
        return Til.builder()
                .tilUser(tilUser)
                .title(title)
                .content(content)
                .tags(List.of(
                        Tag.builder()
                                .tagString("태그1")
                                .build(),
                        Tag.builder()
                                .tagString("태그2")
                                .build()
                ))
                .build();
    }
}
