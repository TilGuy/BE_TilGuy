package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Tag;
import com.tilguys.matilda.til.domain.Til;
import java.util.List;

public record TilDetailResponse(
        Long tilId,
        String title,
        String content,
        List<String> tags
) {

    public static TilDetailResponse fromEntity(Til til) {
        List<String> tags = til.getTags().stream()
                .map(Tag::getTagString)
                .toList();
        return new TilDetailResponse(
                til.getTilId(),
                til.getTitle(),
                til.getContent(),
                tags
        );
    }
}
