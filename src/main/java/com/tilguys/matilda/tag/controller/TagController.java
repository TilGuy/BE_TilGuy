package com.tilguys.matilda.tag.controller;

import com.tilguys.matilda.tag.domain.KeywordTags;
import com.tilguys.matilda.tag.domain.SubTag;
import com.tilguys.matilda.tag.service.TagRelationService;
import com.tilguys.matilda.tag.service.TilTagService;
import com.tilguys.matilda.til.domain.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    private final TagRelationService tagRelationService;

    @GetMapping("/recent")
    public ResponseEntity<KeywordTags> getRecentTags() {
        LocalDate startDay = LocalDate.now().minusDays(TAG_GET_START_DAY);
        List<Tag> tags = tilTagService.getRecentWroteTags(startDay).stream()
                .filter(tag -> tag.getTil().isNotDeleted())
                .toList();

        List<SubTag> subTags = tilTagService.getRecentSubTags(startDay).stream()
                .filter(subTag -> subTag.getTag() != null && subTag.getTag().getTil() != null && subTag.getTag()
                        .getTil().isNotDeleted())
                .toList();
        Map<Tag, List<Tag>> tagRelationMap = tagRelationService.getRecentRelationTagMap();

        for (Tag tag : tagRelationMap.keySet()) {
            if (!tag.getTil().isNotDeleted()) {
                tagRelationMap.remove(tag);
            }
        }
        KeywordTags keywordTag = new KeywordTags(tags, subTags, tagRelationMap);
        return ResponseEntity.ok(keywordTag);
    }
}
