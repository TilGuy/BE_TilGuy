package com.tilguys.matilda.bookmark.service;

import com.tilguys.matilda.bookmark.domain.TilBookmark;
import com.tilguys.matilda.bookmark.dto.ToggleTilBookmarkRequest;
import com.tilguys.matilda.bookmark.repository.TilBookmarkRepository;
import com.tilguys.matilda.common.auth.service.UserService;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.service.TilService;
import com.tilguys.matilda.user.Member;
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
        Member member = userService.getById(memberId);
        if (tilBookmarkRepository.existsByTil_TilId(toggleBookmark.tilId())) {
            tilBookmarkRepository.deleteById(toggleBookmark.tilId());
            return;
        }
        Til tilByTilId = tilService.getTilByTilId(toggleBookmark.tilId());
        tilBookmarkRepository.save(new TilBookmark(null, tilByTilId, member));
    }

    @Transactional
    public List<TilBookmark> userBookmark(Long memberId) {
        return tilBookmarkRepository.findByMember_id(memberId);
    }
}
