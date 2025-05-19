package com.tilguys.matilda.bookmark.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import com.tilguys.matilda.bookmark.domain.TilBookmark;
import com.tilguys.matilda.bookmark.dto.ToggleTilBookmarkRequest;
import com.tilguys.matilda.bookmark.repository.TilBookmarkRepository;
import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.service.TilService;
import com.tilguys.matilda.user.Member;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
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

//의존성 깔끔하게 하는방법 찾아보기 repository까지는 이정도 규모면 h2쓴다고치고

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
    void toggleBookmark() {
        Member member = new Member(null, ProviderInfo.GITHUB, "asdf", Role.USER, "asdf", "asdfasdf");
        Til til = new Til(null, member.getId(), "Asdf", "asdf", LocalDate.now(), true, false, new ArrayList<>());
        em.persist(member);
        em.persist(til);
        em.flush();

        when(userService.getById(member.getId())).thenReturn(member);
        when(tilService.getTilByTilId(til.getTilId())).thenReturn(til);
        // when
        tilBookmarkService.toggleBookmark(new ToggleTilBookmarkRequest(til.getTilId()), member.getId());

        Member findUser = em.find(Member.class, member.getId());
        // then

        List<TilBookmark> bookmarks = tilBookmarkRepository.findByMember_id(findUser.getId());
        assertThat(bookmarks).hasSize(1);
        assertThat(bookmarks.get(0).getTil().getTilId()).isEqualTo(til.getTilId());
    }

    @Test
    @Transactional
    void toggleBookmark_duplicate() {
        // given
        Member member = new Member(1L, ProviderInfo.GITHUB, "asdf", Role.USER, "asdf", "asdfasdf");
        when(userService.getById(1L)).thenReturn(member);

        Til til = new Til(1L, 1L, "Asdf", "asdf", LocalDate.now(), true, false, new ArrayList<>());
        when(tilService.getTilByTilId(1L)).thenReturn(til);
        when(tilService.getTilByTilId(til.getTilId())).thenReturn(til);

        // when
        tilBookmarkService.toggleBookmark(new ToggleTilBookmarkRequest(til.getTilId()), member.getId());

        // then
        assertThatThrownBy(() ->
                tilBookmarkService.toggleBookmark(new ToggleTilBookmarkRequest(til.getTilId()), member.getId())
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 북마크된 TIL입니다");
    }

    @Test
    @Transactional
    void removeBookmark() {
        // given
        Member member = new Member(1L, ProviderInfo.GITHUB, "asdf", Role.USER, "asdf", "asdfasdf");
        when(userService.getById(1L)).thenReturn(member);

        Til til = new Til(1L, 1L, "Asdf", "asdf", LocalDate.now(), true, false, new ArrayList<>());
        when(tilService.getTilByTilId(1L)).thenReturn(til);

        tilBookmarkService.toggleBookmark(new ToggleTilBookmarkRequest(til.getTilId()), member.getId());

        List<TilBookmark> bookmarks = tilBookmarkRepository.findByMember_id(til.getUserId());
        assertThat(bookmarks).hasSize(1);

        // when
        tilBookmarkService.toggleBookmark(new ToggleTilBookmarkRequest(til.getTilId()),
                member.getId());

        // then
        assertThat(tilBookmarkRepository.findByMember_id(member.getId())).isEmpty();
    }
}
