package com.tilguys.matilda.bookmark.service;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import com.tilguys.matilda.bookmark.domain.TilBookmark;
import com.tilguys.matilda.bookmark.dto.AddTilBookmarkRequest;
import com.tilguys.matilda.bookmark.repository.TilBookmarkRepository;
import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.service.TilService;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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
    private EntityManager em;  // EntityManager 추가

    @BeforeEach
    void setUp() {
        tilBookmarkService = new TilBookmarkService(tilBookmarkRepository, userService, tilService);
    }

    @Test
    @Transactional
        // 영속성 컨텍스트 활용
    void addBookmark() {
        TilUser tilUser = new TilUser(null, ProviderInfo.GITHUB, "asdf", Role.USER, "asdf", "asdfasdf");
        Til til = new Til(null, tilUser.getId(), "Asdf", "asdf", LocalDate.now(), true, false, new ArrayList<>());
        em.persist(tilUser);
        em.persist(til);
        em.flush();

        when(userService.getById(tilUser.getId())).thenReturn(tilUser);
        // when
        tilBookmarkService.addBookmark(new AddTilBookmarkRequest(til.getTilId()), tilUser.getId());

        // then
        // 영속성 컨텍스트를 활용하여 직접 조회
        List<TilBookmark> bookmarks = tilBookmarkRepository.findByMember_id(tilUser.getId());
        assertThat(bookmarks).hasSize(1);
        assertThat(bookmarks.get(0).getTil().getTilId()).isEqualTo(til.getTilId());
    }

//    @Test
//    @Transactional
//    void addBookmark_duplicate() {
//        // given
//        TilUser tilUser = new TilUser(1L, ProviderInfo.GITHUB, "asdf", Role.USER, "asdf", "asdfasdf");
//        when(userService.getById(1L)).thenReturn(tilUser);
//
//        Til til = new Til(1L, 1L, "Asdf", "asdf", LocalDate.now(), true, false, new ArrayList<>());
//        when(tilService.getTilByTilId(1L)).thenReturn(til);
//
//        // when
//        tilBookmarkService.addBookmark(new AddTilBookmarkRequest(til.getTilId()), tilUser.getId());
//
//        // then
//        assertThatThrownBy(() ->
//                tilBookmarkService.addBookmark(new AddTilBookmarkRequest(til.getTilId()), tilUser.getId())
//        ).isInstanceOf(IllegalStateException.class)
//                .hasMessageContaining("이미 북마크된 TIL입니다");
//    }
//
//    @Test
//    @Transactional
//    void removeBookmark() {
//        // given
//        TilUser tilUser = new TilUser(1L, ProviderInfo.GITHUB, "asdf", Role.USER, "asdf", "asdfasdf");
//        when(userService.getById(1L)).thenReturn(tilUser);
//
//        Til til = new Til(1L, 1L, "Asdf", "asdf", LocalDate.now(), true, false, new ArrayList<>());
//        when(tilService.getTilByTilId(1L)).thenReturn(til);
//
//        // when
//        tilBookmarkService.addBookmark(new AddTilBookmarkRequest(til.getTilId()), tilUser.getId());
//        tilBookmarkService.removeBookmark(til.getTilId(), tilUser.getId());
//
//        // then
//        List<TilBookmark> bookmarks = tilBookmarkRepository.findByUserId(tilUser.getId());
//        assertThat(bookmarks).isEmpty();
//    }
}
