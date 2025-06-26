package com.tilguys.matilda.tag.controller;

import com.tilguys.matilda.tag.cache.RecentTilTagsCache;
import com.tilguys.matilda.tag.domain.TilTagRelations;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagController {

    private final RecentTilTagsCache recentTilTagsCache;

    @GetMapping("/recent")
    public ResponseEntity<TilTagRelations> getRecentTags() {
        LocalTime start = LocalTime.now();
        TilTagRelations recentTagRelations = recentTilTagsCache.getRecentTagRelations();
        log.debug("{}초 소요됨", LocalTime.now().toSecondOfDay() - start.toSecondOfDay());
        return ResponseEntity.ok(recentTagRelations);
    }
}
