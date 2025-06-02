package com.tilguys.matilda.bookmark.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import com.tilguys.matilda.bookmark.domain.TilBookmark;
import com.tilguys.matilda.bookmark.dto.ToggleTilBookmarkRequest;
import com.tilguys.matilda.bookmark.repository.TilBookmarkRepository;
import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.service.TilService;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DataJpaTest
class TilBookmarkServiceTest {

    private TilBookmarkService tilBookmarkService;

    @Autowired
    private TilBookmarkRepository tilBookmarkRepository;

    @Mock
    private UserService userService;

    @Mock
    private TilService tilService;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        tilBookmarkService = new TilBookmarkService(tilBookmarkRepository, userService, tilService);
    }

    @Test
    void toggleBookmark() {
        TilUser member = new TilUser(null, ProviderInfo.GITHUB, "asdf", Role.USER, "asdf", "asdfasdf");
        Til til = new Til(null, member, "Asdf", "asdf", LocalDate.now(), true, false, new ArrayList<>());
        em.persist(member);
        em.persist(til);
        em.flush();

        when(userService.getById(member.getId())).thenReturn(member);
        when(tilService.getTilByTilId(til.getTilId())).thenReturn(til);
        // when
        tilBookmarkService.toggleBookmark(new ToggleTilBookmarkRequest(til.getTilId()), member.getId());

        TilUser findUser = em.find(TilUser.class, member.getId());
        // then

        List<TilBookmark> bookmarks = tilBookmarkRepository.findByTilUser_id(findUser.getId());
        assertThat(bookmarks).hasSize(1);
        assertThat(bookmarks.get(0).getTil().getTilId()).isEqualTo(til.getTilId());
    }

    @Test
    void removeBookmark() {
        TilUser member = new TilUser(null, ProviderInfo.GITHUB, "asdf", Role.USER, "asdf", "asdfasdf");
        Til til = new Til(null, member, "Asdf", "asdf", LocalDate.now(), true, false, new ArrayList<>());

        em.persist(member);
        em.persist(til);
        em.flush();

        when(userService.getById(member.getId())).thenReturn(member);
        when(tilService.getTilByTilId(til.getTilId())).thenReturn(til);
        // when
        tilBookmarkService.toggleBookmark(new ToggleTilBookmarkRequest(til.getTilId()), member.getId());

        TilUser findUser = em.find(TilUser.class, member.getId());
        // then

        List<TilBookmark> bookmarks = tilBookmarkRepository.findByTilUser_id(findUser.getId());
        assertThat(bookmarks).hasSize(1);
        assertThat(bookmarks.get(0).getTil().getTilId()).isEqualTo(til.getTilId());

        tilBookmarkService.toggleBookmark(new ToggleTilBookmarkRequest(til.getTilId()), member.getId());
        assertThat(tilBookmarkRepository.findByTilUser_id(findUser.getId())).hasSize(0);
    }
}
