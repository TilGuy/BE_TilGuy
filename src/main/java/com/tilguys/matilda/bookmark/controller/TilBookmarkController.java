package com.tilguys.matilda.bookmark.controller;

import com.tilguys.matilda.bookmark.domain.TilBookmark;
import com.tilguys.matilda.bookmark.dto.TilBookmarkResponse;
import com.tilguys.matilda.bookmark.dto.ToggleTilBookmarkRequest;
import com.tilguys.matilda.bookmark.service.TilBookmarkService;
import com.tilguys.matilda.common.auth.SimpleUserInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/bookmarks")
@RestController
@RequiredArgsConstructor
public class TilBookmarkController {

    private final TilBookmarkService tilBookmarkService;

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleBookmark(
            @RequestBody final ToggleTilBookmarkRequest request,
            @AuthenticationPrincipal final SimpleUserInfo simpleUserInfo) {
        tilBookmarkService.toggleBookmark(request, simpleUserInfo.id());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> getUserBookmarks(
            @AuthenticationPrincipal final SimpleUserInfo simpleUserInfo) {
        List<TilBookmark> bookmarks = tilBookmarkService.userBookmark(simpleUserInfo.id());

        List<TilBookmarkResponse> tilBookmarkResponses = bookmarks.stream()
                .map((bookmark) -> new TilBookmarkResponse(bookmark.getId(), bookmark.getTil().getTilId()))
                .toList();

        return ResponseEntity.ok(tilBookmarkResponses);
    }
}
