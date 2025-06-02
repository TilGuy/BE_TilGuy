package com.tilguys.matilda.bookmark.service;

import com.tilguys.matilda.bookmark.domain.TilBookmark;
import com.tilguys.matilda.bookmark.dto.ToggleTilBookmarkRequest;
import com.tilguys.matilda.bookmark.repository.TilBookmarkRepository;
import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.service.TilService;
import com.tilguys.matilda.user.TilUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TilBookmarkService {

    private final TilBookmarkRepository tilBookmarkRepository;
    private final UserService userService;
    private final TilService tilService;

    @Transactional
    public void toggleBookmark(ToggleTilBookmarkRequest toggleBookmark, Long memberId) {
        TilUser tilUser = userService.getById(memberId);
        if (tilBookmarkRepository.existsByTil_TilId(toggleBookmark.tilId())) {
            tilBookmarkRepository.deleteByTil_TilId(toggleBookmark.tilId());
            return;
        }
        Til tilByTilId = tilService.getTilByTilId(toggleBookmark.tilId());
        tilBookmarkRepository.save(new TilBookmark(null, tilByTilId, tilUser));
    }

    @Transactional
    public List<Til> userBookmarkTils(Long memberId) {
        List<TilBookmark> tilBookmarks = tilBookmarkRepository.findByTilUser_id(memberId);

        List<Long> tilIds = tilBookmarks.stream()
                .map(currentTilBookmark -> currentTilBookmark.getTil().getTilId())
                .toList();

        return tilService.getTilsByIds(tilIds);
    }
}
