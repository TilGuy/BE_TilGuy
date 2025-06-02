package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Til;

public record TilWithUserResponse(
        Long id,
        String title,
        String content,
        TagsResponse tags,
        ReferencesResponse references,
        String nickname,
        String avatarUrl
) {

    public TilWithUserResponse(Til til) {
        this(
                til.getTilId(),
                til.getTitle(),
                til.getContent(),
                new TagsResponse(til.getTags()),
                new ReferencesResponse(til.getReferences()),
                til.getTilUser().getNickname(),
                til.getTilUser().getAvatarUrl()
        );
    }
}
