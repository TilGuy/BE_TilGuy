package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Til;

public record TilDetailResponse(
        Long tilId,
        String title,
        String content,
        TagsResponse tags,
        ReferencesResponse references
) {

    public static TilDetailResponse fromEntity(Til til) {
        return new TilDetailResponse(
                til.getTilId(),
                til.getTitle(),
                til.getContent(),
                new TagsResponse(til.getTags()),
                new ReferencesResponse(til.getReferences())
        );
    }
}
