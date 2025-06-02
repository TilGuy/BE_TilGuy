package com.tilguys.matilda.tag.controller;

import com.tilguys.matilda.tag.service.TilTagService;
import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.dto.TagsResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagController {

    private static final int TAG_GET_START_DAY = 7;
    private final TilTagService tilTagService;

    @GetMapping("/recent")
    public ResponseEntity<List<String>> getRecentTags() {
        LocalDate startDay = LocalDate.now().minusDays(TAG_GET_START_DAY);
        List<Tag> tags = tilTagService.getRecentWroteTags(startDay);
        TagsResponse tagsResponse = new TagsResponse(tags);
        List<String> tagStrings = tagsResponse.getTags();
        return ResponseEntity.ok(tagStrings);
    }
}
