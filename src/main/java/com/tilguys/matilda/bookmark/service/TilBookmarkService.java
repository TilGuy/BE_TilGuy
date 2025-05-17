package com.tilguys.matilda.bookmark.service;

import com.tilguys.matilda.bookmark.domain.TilBookmark;
import com.tilguys.matilda.bookmark.dto.AddTilBookmarkRequest;
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
    public void addBookmark(AddTilBookmarkRequest addTilBookmark, Long memberId) {
        TilUser tilUser = userService.getById(memberId);
        Til tilByTilId = tilService.getTilByTilId(addTilBookmark.tilId());
        tilBookmarkRepository.save(new TilBookmark(null, tilByTilId, tilUser));
    }

    @Transactional
    public List<TilBookmark> userBookmark(Long memberId) {
        return tilBookmarkRepository.findByMember_id(memberId);
    }
}
