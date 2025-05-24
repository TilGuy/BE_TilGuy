package com.tilguys.matilda.til.dto;

import com.tilguys.matilda.til.domain.Til;

public record TilWithUserResponse(
        String title,
        String content,
        TagsResponse tags,
        String nickname,
        String avatarUrl
) {

    public TilWithUserResponse(Til til) {
        this(
                til.getTitle(),
                til.getContent(),
                new TagsResponse(til.getTags()),
                til.getTilUser().getNickname(),
                til.getTilUser().getAvatarUrl()
        );
    }
}
